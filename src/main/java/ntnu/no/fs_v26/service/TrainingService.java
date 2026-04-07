package ntnu.no.fs_v26.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.dto.CreateQuizQuestionRequest;
import ntnu.no.fs_v26.dto.EmployeeOptionResponse;
import ntnu.no.fs_v26.dto.QuizSubmissionRequest;
import ntnu.no.fs_v26.dto.TrainingQuizResultResponse;
import ntnu.no.fs_v26.dto.TrainingStatusResponse;
import ntnu.no.fs_v26.model.Certification;
import ntnu.no.fs_v26.model.DocumentAcknowledgement;
import ntnu.no.fs_v26.model.PracticalTrainingSignOff;
import ntnu.no.fs_v26.model.Role;
import ntnu.no.fs_v26.model.TrainingAttempt;
import ntnu.no.fs_v26.model.TrainingCompletionType;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.TrainingDocumentType;
import ntnu.no.fs_v26.model.TrainingQuizQuestion;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.CertificationRepository;
import ntnu.no.fs_v26.repository.DocumentAcknowledgementRepository;
import ntnu.no.fs_v26.repository.PracticalTrainingSignOffRepository;
import ntnu.no.fs_v26.repository.TrainingAttemptRepository;
import ntnu.no.fs_v26.repository.TrainingDocumentRepository;
import ntnu.no.fs_v26.repository.TrainingQuizQuestionRepository;
import ntnu.no.fs_v26.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingDocumentRepository documentRepository;
    private final TrainingQuizQuestionRepository questionRepository;
    private final TrainingAttemptRepository attemptRepository;
    private final CertificationRepository certificationRepository;
    private final DocumentAcknowledgementRepository acknowledgementRepository;
    private final PracticalTrainingSignOffRepository signOffRepository;
    private final UserRepository userRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<TrainingDocument> getDocumentsByType(TrainingDocumentType type) {
        User user = getCurrentUser();
        return documentRepository.findByOrganizationAndTypeAndActiveTrue(user.getOrganization(), type);
    }

    public TrainingDocument getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    public List<Certification> getMyCertifications() {
        User user = getCurrentUser();

        if (user.getRole() != Role.EMPLOYEE) {
            return List.of();
        }

        return certificationRepository.findByUser(user);
    }

    public List<Certification> getOrganizationCertifications() {
        User user = getCurrentUser();

        if (!(user.getRole() == Role.MANAGER || user.getRole() == Role.ADMIN)) {
            throw new AccessDeniedException("Only managers and admins can view team certifications.");
        }

        return certificationRepository.findByOrganization(user.getOrganization());
    }

    public List<DocumentAcknowledgement> getMyAcknowledgements() {
        User user = getCurrentUser();

        if (user.getRole() != Role.EMPLOYEE) {
            return List.of();
        }

        return acknowledgementRepository.findByUser(user);
    }

    public List<DocumentAcknowledgement> getTeamAcknowledgements() {
        User user = getCurrentUser();

        if (!(user.getRole() == Role.MANAGER || user.getRole() == Role.ADMIN)) {
            throw new AccessDeniedException("Only managers and admins can view team acknowledgements.");
        }

        return acknowledgementRepository.findAll().stream()
                .filter(a -> a.getUser().getOrganization() != null)
                .filter(a -> a.getUser().getOrganization().getId().equals(user.getOrganization().getId()))
                .toList();
    }

    public List<EmployeeOptionResponse> getOrganizationEmployees() {
        User currentUser = getCurrentUser();

        if (!(currentUser.getRole() == Role.MANAGER || currentUser.getRole() == Role.ADMIN)) {
            throw new AccessDeniedException("Only managers and admins can view employees.");
        }

        return userRepository.findByOrganizationAndRole(currentUser.getOrganization(), Role.EMPLOYEE)
                .stream()
                .map(user -> EmployeeOptionResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .build())
                .toList();
    }

    public TrainingDocument createDocument(TrainingDocument document) {
        User user = getCurrentUser();

        if (!(user.getRole() == Role.MANAGER || user.getRole() == Role.ADMIN)) {
            throw new AccessDeniedException("Only managers and admins can create training documents.");
        }

        if (user.getOrganization() == null) {
            throw new RuntimeException("Current user is not connected to an organization.");
        }

        if (document.getType() == TrainingDocumentType.POLICY) {
            document.setCompletionType(TrainingCompletionType.READ_ACKNOWLEDGE);
        }

        if (document.getType() == TrainingDocumentType.TRAINING
                && document.getCompletionType() == TrainingCompletionType.READ_ACKNOWLEDGE) {
            throw new IllegalArgumentException("Training materials must use QUIZ or PRACTICAL_SIGN_OFF.");
        }

        document.setId(null);
        document.setOrganization(user.getOrganization());
        document.setActive(true);

        if (document.getContent() == null || document.getContent().isBlank()) {
            document.setContent("No content provided.");
        }

        return documentRepository.save(document);
    }

    public TrainingDocument uploadDocument(
            MultipartFile file,
            String title,
            String description,
            String content,
            TrainingDocumentType type,
            TrainingCompletionType completionType) throws Exception {

        User user = getCurrentUser();

        if (!(user.getRole() == Role.MANAGER || user.getRole() == Role.ADMIN)) {
            throw new AccessDeniedException("Only managers and admins can upload documents.");
        }

        if (type == TrainingDocumentType.POLICY) {
            completionType = TrainingCompletionType.READ_ACKNOWLEDGE;
        }

        if (type == TrainingDocumentType.TRAINING
                && completionType == TrainingCompletionType.READ_ACKNOWLEDGE) {
            throw new IllegalArgumentException("Training materials must use QUIZ or PRACTICAL_SIGN_OFF.");
        }

        String uploadDir = "uploads/";
        String storedFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, storedFileName);

        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());

        TrainingDocument document = TrainingDocument.builder()
                .title(title)
                .description(description)
                .content(content != null ? content : "")
                .type(type)
                .completionType(completionType)
                .organization(user.getOrganization())
                .active(true)
                .fileName(file.getOriginalFilename())
                .filePath(path.toString())
                .build();

        return documentRepository.save(document);
    }

    public List<TrainingQuizQuestion> getQuizQuestions(Long documentId) {
        User user = getCurrentUser();
        TrainingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (document.getCompletionType() != TrainingCompletionType.QUIZ) {
            return List.of();
        }

        return questionRepository.findByDocument(document);
    }

    public TrainingQuizQuestion addQuizQuestion(Long documentId, CreateQuizQuestionRequest request) {
        User user = getCurrentUser();

        if (!(user.getRole() == Role.MANAGER || user.getRole() == Role.ADMIN)) {
            throw new AccessDeniedException("Only managers and admins can add quiz questions.");
        }

        TrainingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (document.getType() != TrainingDocumentType.TRAINING) {
            throw new IllegalArgumentException("Quiz questions can only be added to training documents.");
        }

        if (document.getCompletionType() != TrainingCompletionType.QUIZ) {
            throw new IllegalArgumentException("Quiz questions can only be added to quiz-based training.");
        }

        String correctAnswer = request.getCorrectAnswer() != null
                ? request.getCorrectAnswer().trim().toUpperCase()
                : "";

        if (!List.of("A", "B", "C", "D").contains(correctAnswer)) {
            throw new IllegalArgumentException("Correct answer must be A, B, C or D.");
        }

        TrainingQuizQuestion question = TrainingQuizQuestion.builder()
                .document(document)
                .question(request.getQuestion())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .correctAnswer(correctAnswer)
                .build();

        return questionRepository.save(question);
    }

    public TrainingQuizResultResponse submitQuiz(Long documentId, QuizSubmissionRequest request) {
        User user = getCurrentUser();

        if (user.getRole() != Role.EMPLOYEE) {
            throw new AccessDeniedException("Only employees can submit quiz answers.");
        }

        TrainingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (document.getType() != TrainingDocumentType.TRAINING) {
            throw new IllegalArgumentException("Only training materials can have quizzes.");
        }

        if (document.getCompletionType() != TrainingCompletionType.QUIZ) {
            throw new IllegalArgumentException("This training is not quiz-based.");
        }

        List<TrainingQuizQuestion> questions = questionRepository.findByDocument(document);

        if (questions.isEmpty()) {
            throw new IllegalArgumentException("No quiz questions found for this training.");
        }

        Map<Long, String> answers = request.getAnswers();
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("No quiz answers submitted.");
        }

        int correct = 0;

        for (TrainingQuizQuestion question : questions) {
            String submitted = answers.get(question.getId());

            if (submitted != null && submitted.trim().equalsIgnoreCase(question.getCorrectAnswer())) {
                correct++;
            }
        }

        int total = questions.size();
        boolean passed = correct == total;

        TrainingAttempt attempt = TrainingAttempt.builder()
                .document(document)
                .user(user)
                .score(correct)
                .totalQuestions(total)
                .passed(passed)
                .build();

        attemptRepository.save(attempt);

        boolean certificationCreated = false;

        if (passed && certificationRepository.findByUserAndDocument(user, document).isEmpty()) {
            Certification certification = Certification.builder()
                    .document(document)
                    .user(user)
                    .organization(user.getOrganization())
                    .title(document.getTitle() + " Certification")
                    .build();

            certificationRepository.save(certification);
            certificationCreated = true;
        }

        return TrainingQuizResultResponse.builder()
                .score(correct)
                .totalQuestions(total)
                .passed(passed)
                .certificationCreated(certificationCreated)
                .message(
                        passed
                                ? "Quiz passed. Certification awarded."
                                : "Quiz not passed. You must answer all questions correctly.")
                .build();
    }

    public void acknowledgeDocument(Long documentId) {
        User user = getCurrentUser();

        if (user.getRole() != Role.EMPLOYEE) {
            throw new AccessDeniedException("Only employees can acknowledge policies.");
        }

        TrainingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (document.getType() != TrainingDocumentType.POLICY) {
            throw new IllegalArgumentException("Only policy documents can be acknowledged.");
        }

        if (document.getCompletionType() != TrainingCompletionType.READ_ACKNOWLEDGE) {
            throw new IllegalArgumentException("This document is not acknowledge-based.");
        }

        DocumentAcknowledgement acknowledgement = acknowledgementRepository
                .findByUserAndDocument(user, document)
                .orElse(
                        DocumentAcknowledgement.builder()
                                .document(document)
                                .user(user)
                                .build());

        acknowledgement.setAcknowledged(true);
        acknowledgement.setAcknowledgedAt(LocalDateTime.now());

        acknowledgementRepository.save(acknowledgement);
    }

    public void approvePracticalTraining(Long documentId, Long employeeId, String note) {
        User manager = getCurrentUser();

        if (!(manager.getRole() == Role.MANAGER || manager.getRole() == Role.ADMIN)) {
            throw new AccessDeniedException("Only managers and admins can approve practical training.");
        }

        TrainingDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (document.getCompletionType() != TrainingCompletionType.PRACTICAL_SIGN_OFF) {
            throw new IllegalArgumentException("This document is not practical sign-off based.");
        }

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getOrganization().getId().equals(manager.getOrganization().getId())) {
            throw new AccessDeniedException("Employee is not in your organization.");
        }

        PracticalTrainingSignOff signOff = signOffRepository.findByEmployeeAndDocument(employee, document)
                .orElse(
                        PracticalTrainingSignOff.builder()
                                .document(document)
                                .employee(employee)
                                .build());

        signOff.setApproved(true);
        signOff.setApprovedBy(manager);
        signOff.setNote(note);
        signOff.setApprovedAt(LocalDateTime.now());

        signOffRepository.save(signOff);

        if (certificationRepository.findByUserAndDocument(employee, document).isEmpty()) {
            Certification certification = Certification.builder()
                    .document(document)
                    .user(employee)
                    .organization(employee.getOrganization())
                    .title(document.getTitle() + " Certification")
                    .build();

            certificationRepository.save(certification);
        }
    }

    public List<TrainingStatusResponse> getMyTrainingStatuses() {
        User user = getCurrentUser();

        if (user.getRole() != Role.EMPLOYEE) {
            return List.of();
        }

        List<TrainingDocument> documents = documentRepository.findByOrganizationAndActiveTrue(user.getOrganization());

        return documents.stream().map(document -> {
            String status = "NOT_STARTED";

            if (document.getType() == TrainingDocumentType.POLICY
                    && document.getCompletionType() == TrainingCompletionType.READ_ACKNOWLEDGE) {

                boolean acknowledged = acknowledgementRepository
                        .findByUserAndDocument(user, document)
                        .map(DocumentAcknowledgement::isAcknowledged)
                        .orElse(false);

                status = acknowledged ? "ACKNOWLEDGED" : "NOT_STARTED";
            }

            if (document.getType() == TrainingDocumentType.TRAINING
                    && document.getCompletionType() == TrainingCompletionType.QUIZ) {

                var latestAttempt = attemptRepository
                        .findTopByUserAndDocumentOrderByCompletedAtDesc(user, document);

                if (latestAttempt.isPresent()) {
                    status = latestAttempt.get().isPassed() ? "PASSED" : "NOT_PASSED";
                }
            }

            if (document.getType() == TrainingDocumentType.TRAINING
                    && document.getCompletionType() == TrainingCompletionType.PRACTICAL_SIGN_OFF) {

                boolean approved = signOffRepository
                        .findByEmployeeAndDocument(user, document)
                        .map(PracticalTrainingSignOff::isApproved)
                        .orElse(false);

                status = approved ? "APPROVED" : "PENDING_MANAGER_APPROVAL";
            }

            return TrainingStatusResponse.builder()
                    .documentId(document.getId())
                    .status(status)
                    .build();
        }).toList();
    }
}