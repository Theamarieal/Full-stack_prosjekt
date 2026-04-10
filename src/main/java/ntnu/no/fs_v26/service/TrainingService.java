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

/**
 * Service class for training document and certification management.
 *
 * <p>Handles all training-related operations including document retrieval and upload,
 * quiz management, acknowledgements, practical training sign-offs, and certification tracking.
 *
 * <p>Three completion types are supported:
 * <ul>
 *   <li>{@link TrainingCompletionType#READ_ACKNOWLEDGE} – for policy documents requiring acknowledgement</li>
 *   <li>{@link TrainingCompletionType#QUIZ} – for training materials with a quiz, awards a certification on full pass</li>
 *   <li>{@link TrainingCompletionType#PRACTICAL_SIGN_OFF} – for hands-on training requiring manager approval</li>
 * </ul>
 *
 * <p>Role restrictions are enforced per method. Employees complete training;
 * managers and admins upload documents, add questions, and approve practical training.
 */
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

    /**
     * Returns the currently authenticated user from the Spring Security context.
     *
     * @return the authenticated {@link User}
     * @throws RuntimeException if the user cannot be found in the database
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Returns all active training documents of the given type for the current user's organization.
     *
     * @param type the document type to filter by (POLICY or TRAINING)
     * @return a list of matching active {@link TrainingDocument} objects
     */
    public List<TrainingDocument> getDocumentsByType(TrainingDocumentType type) {
        User user = getCurrentUser();
        return documentRepository.findByOrganizationAndTypeAndActiveTrue(user.getOrganization(), type);
    }

    /**
     * Returns a training document by its ID.
     *
     * @param id the document ID
     * @return the {@link TrainingDocument}
     * @throws RuntimeException if no document with the given ID exists
     */
    public TrainingDocument getDocumentById(Long id) {
        return documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    /**
     * Returns all certifications earned by the currently authenticated employee.
     * Returns an empty list for managers and admins.
     *
     * @return a list of {@link Certification} objects for the current user
     */
    public List<Certification> getMyCertifications() {
        User user = getCurrentUser();
        if (user.getRole() != Role.EMPLOYEE) return List.of();
        return certificationRepository.findByUser(user);
    }

    /**
     * Returns all certifications earned within the current user's organization.
     * Only accessible to managers and admins.
     *
     * @return a list of {@link Certification} objects for the organization
     * @throws AccessDeniedException if the current user is not a manager or admin
     */
    public List<Certification> getOrganizationCertifications() {
        User user = getCurrentUser();
        if (!(user.getRole() == Role.MANAGER || user.getRole() == Role.ADMIN)) {
            throw new AccessDeniedException("Only managers and admins can view team certifications.");
        }
        return certificationRepository.findByOrganization(user.getOrganization());
    }

    /**
     * Returns all document acknowledgements made by the currently authenticated employee.
     * Returns an empty list for managers and admins.
     *
     * @return a list of {@link DocumentAcknowledgement} objects for the current user
     */
    public List<DocumentAcknowledgement> getMyAcknowledgements() {
        User user = getCurrentUser();
        if (user.getRole() != Role.EMPLOYEE) return List.of();
        return acknowledgementRepository.findByUser(user);
    }

    /**
     * Returns all document acknowledgements across the current user's organization.
     * Only accessible to managers and admins.
     *
     * @return a list of {@link DocumentAcknowledgement} objects for the organization
     * @throws AccessDeniedException if the current user is not a manager or admin
     */
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

    /**
     * Returns a list of employee users in the current user's organization.
     * Used for populating the employee selector in practical training sign-off forms.
     * Only accessible to managers and admins.
     *
     * @return a list of {@link EmployeeOptionResponse} objects
     * @throws AccessDeniedException if the current user is not a manager or admin
     */
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

    /**
     * Creates a new training document using metadata only (no file upload).
     * Policies are automatically assigned the READ_ACKNOWLEDGE completion type.
     * Training materials cannot use READ_ACKNOWLEDGE.
     * Only accessible to managers and admins.
     *
     * @param document the document entity to save
     * @return the saved {@link TrainingDocument}
     * @throws AccessDeniedException    if the current user is not a manager or admin
     * @throws IllegalArgumentException if a training material is assigned READ_ACKNOWLEDGE
     */
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

    /**
     * Uploads a training document as a file and saves it to the {@code uploads/} directory.
     * The file path and original filename are stored on the document entity.
     * Only accessible to managers and admins.
     *
     * @param file           the uploaded file
     * @param title          the display title of the document
     * @param description    a description of the document's content
     * @param content        optional text content
     * @param type           the document type (POLICY or TRAINING)
     * @param completionType how completion is tracked (QUIZ, ACKNOWLEDGEMENT, or PRACTICAL_SIGN_OFF)
     * @return the saved {@link TrainingDocument}
     * @throws AccessDeniedException    if the current user is not a manager or admin
     * @throws IllegalArgumentException if a training material is assigned READ_ACKNOWLEDGE
     * @throws Exception                if the file cannot be written to disk
     */
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

    /**
     * Returns the quiz questions for a specific training document.
     * Verifies that the document belongs to the current user's organization.
     *
     * @param documentId the ID of the training document
     * @return a list of {@link TrainingQuizQuestion} objects
     * @throws RuntimeException      if the document is not found
     * @throws AccessDeniedException if the document belongs to a different organization
     */
    public List<TrainingQuizQuestion> getQuizQuestions(Long documentId) {
        User user = getCurrentUser();
        TrainingDocument document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));
        if (!document.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied");
        }
        return questionRepository.findByDocument(document);
    }

    /**
     * Adds a new quiz question to a training document.
     * Only accessible to managers and admins.
     *
     * @param documentId the ID of the document to add the question to
     * @param request    the question data including text, options, and correct answer
     * @return the saved {@link TrainingQuizQuestion}
     * @throws AccessDeniedException if the current user is not a manager or admin
     * @throws RuntimeException      if the document is not found
     */
    public TrainingQuizQuestion addQuizQuestion(Long documentId, CreateQuizQuestionRequest request) {
        User user = getCurrentUser();
        if (!(user.getRole() == Role.MANAGER || user.getRole() == Role.ADMIN)) {
            throw new AccessDeniedException("Only managers and admins can add quiz questions.");
        }
        TrainingDocument document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        TrainingQuizQuestion question = TrainingQuizQuestion.builder()
            .document(document)
            .question(request.getQuestion())
            .optionA(request.getOptionA())
            .optionB(request.getOptionB())
            .optionC(request.getOptionC())
            .optionD(request.getOptionD())
            .correctAnswer(request.getCorrectAnswer())
            .build();

        return questionRepository.save(question);
    }

    /**
     * Submits quiz answers for a training document and evaluates the result.
     *
     * <p>All questions must be answered correctly to pass. On a passing attempt,
     * a {@link Certification} is awarded if the user does not already have one
     * for this document.
     *
     * @param documentId the ID of the training document being assessed
     * @param request    the submission containing the user's answers mapped by question ID
     * @return a {@link TrainingQuizResultResponse} indicating score, pass/fail, and certification status
     * @throws RuntimeException         if the document is not found or has no quiz questions
     * @throws IllegalArgumentException if the document is not quiz-based or no answers were submitted
     */
    public TrainingQuizResultResponse submitQuiz(Long documentId, QuizSubmissionRequest request) {
        User user = getCurrentUser();
        TrainingDocument document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied");
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
            .message(passed
                ? "Quiz passed. Certification awarded."
                : "Quiz not passed. You must answer all questions correctly.")
            .build();
    }

    /**
     * Records that the currently authenticated employee has acknowledged a policy document.
     * Only employees can acknowledge documents. Prevents duplicate acknowledgements.
     *
     * @param documentId the ID of the policy document to acknowledge
     * @throws AccessDeniedException    if the current user is not an employee
     * @throws RuntimeException         if the document is not found
     * @throws IllegalArgumentException if the document is not a policy or not acknowledge-based
     */
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
            .orElse(DocumentAcknowledgement.builder()
                .document(document)
                .user(user)
                .build());

        acknowledgement.setAcknowledged(true);
        acknowledgement.setAcknowledgedAt(LocalDateTime.now());
        acknowledgementRepository.save(acknowledgement);
    }

    /**
     * Records a practical training sign-off for an employee, approved by a manager.
     *
     * <p>Verifies that both the document and the employee belong to the manager's organization.
     * If the employee does not yet have a certification for this document, one is awarded automatically.
     *
     * @param documentId the ID of the training document the sign-off relates to
     * @param employeeId the ID of the employee being signed off
     * @param note       an optional note from the approving manager
     * @throws AccessDeniedException    if the current user is not a manager or admin,
     *                                  or if the employee belongs to a different organization
     * @throws RuntimeException         if the document or employee is not found
     * @throws IllegalArgumentException if the document is not practical sign-off based
     */
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
            .orElse(PracticalTrainingSignOff.builder()
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

    /**
     * Returns the training completion status for each active document in the organization,
     * as seen by the currently authenticated employee.
     *
     * <p>Status values:
     * <ul>
     *   <li>{@code NOT_STARTED} – no activity recorded</li>
     *   <li>{@code ACKNOWLEDGED} – policy has been acknowledged</li>
     *   <li>{@code PASSED} / {@code NOT_PASSED} – quiz result</li>
     *   <li>{@code APPROVED} / {@code PENDING_MANAGER_APPROVAL} – practical sign-off status</li>
     * </ul>
     *
     * Returns an empty list for managers and admins.
     *
     * @return a list of {@link TrainingStatusResponse} objects for the current employee
     */
    public List<TrainingStatusResponse> getMyTrainingStatuses() {
        User user = getCurrentUser();
        if (user.getRole() != Role.EMPLOYEE) return List.of();

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