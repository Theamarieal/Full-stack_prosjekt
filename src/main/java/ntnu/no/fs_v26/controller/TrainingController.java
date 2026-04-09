package ntnu.no.fs_v26.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.dto.CreateQuizQuestionRequest;
import ntnu.no.fs_v26.dto.EmployeeOptionResponse;
import ntnu.no.fs_v26.dto.PracticalSignOffRequest;
import ntnu.no.fs_v26.dto.QuizSubmissionRequest;
import ntnu.no.fs_v26.dto.TrainingQuizResultResponse;
import ntnu.no.fs_v26.dto.TrainingStatusResponse;
import ntnu.no.fs_v26.model.Certification;
import ntnu.no.fs_v26.model.DocumentAcknowledgement;
import ntnu.no.fs_v26.model.TrainingCompletionType;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.TrainingDocumentType;
import ntnu.no.fs_v26.model.TrainingQuizQuestion;
import ntnu.no.fs_v26.service.TrainingService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for training document and certification management.
 *
 * <p>Provides endpoints for managing training materials and policies,
 * including document upload and download, quiz management, acknowledgements,
 * practical sign-offs, and certification tracking.
 *
 * <p>Used by both employees (reading and completing training) and managers/admins
 * (uploading documents, signing off practical training, reviewing team progress).
 *
 * <p>Base path: {@code /api/v1/training}
 */
@RestController
@RequestMapping("/api/v1/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    /**
     * Returns all policy documents available in the system.
     *
     * @return a list of {@link TrainingDocument} entries of type POLICY
     */
    @GetMapping("/policies")
    public List<TrainingDocument> getPolicies() {
        return trainingService.getDocumentsByType(TrainingDocumentType.POLICY);
    }

    /**
     * Returns all training material documents available in the system.
     *
     * @return a list of {@link TrainingDocument} entries of type TRAINING
     */
    @GetMapping("/materials")
    public List<TrainingDocument> getMaterials() {
        return trainingService.getDocumentsByType(TrainingDocumentType.TRAINING);
    }

    /**
     * Returns the training completion statuses for the currently authenticated user.
     *
     * @return a list of {@link TrainingStatusResponse} objects for the current user
     */
    @GetMapping("/statuses/me")
    public List<TrainingStatusResponse> getMyTrainingStatuses() {
        return trainingService.getMyTrainingStatuses();
    }

    /**
     * Returns certifications earned by the currently authenticated user.
     *
     * @return a list of {@link Certification} objects for the current user
     */
    @GetMapping("/certifications/me")
    public List<Certification> getMyCertifications() {
        return trainingService.getMyCertifications();
    }

    /**
     * Returns certifications for all members of the current organization.
     * Intended for use by managers and administrators.
     *
     * @return a list of {@link Certification} objects for the organization
     */
    @GetMapping("/certifications/team")
    public List<Certification> getTeamCertifications() {
        return trainingService.getOrganizationCertifications();
    }

    /**
     * Returns all document acknowledgements made by the currently authenticated user.
     *
     * @return a list of {@link DocumentAcknowledgement} objects for the current user
     */
    @GetMapping("/acknowledgements/me")
    public List<DocumentAcknowledgement> getMyAcknowledgements() {
        return trainingService.getMyAcknowledgements();
    }

    /**
     * Returns all document acknowledgements across the current organization.
     * Intended for use by managers and administrators.
     *
     * @return a list of {@link DocumentAcknowledgement} objects for the organization
     */
    @GetMapping("/acknowledgements/team")
    public List<DocumentAcknowledgement> getTeamAcknowledgements() {
        return trainingService.getTeamAcknowledgements();
    }

    /**
     * Returns a list of employees in the current organization.
     * Used for selecting employees in practical training sign-off forms.
     *
     * @return a list of {@link EmployeeOptionResponse} objects
     */
    @GetMapping("/employees")
    public List<EmployeeOptionResponse> getOrganizationEmployees() {
        return trainingService.getOrganizationEmployees();
    }

    /**
     * Creates a new training document using metadata only (no file upload).
     *
     * @param document the training document entity containing title, type, and completion type
     * @return the saved {@link TrainingDocument}
     */
    @PostMapping("/documents")
    public TrainingDocument createDocument(@RequestBody TrainingDocument document) {
        return trainingService.createDocument(document);
    }

    /**
     * Uploads a training document as a file (multipart/form-data).
     *
     * @param file           the file to upload (PDF, TXT, etc.)
     * @param title          the display title of the document
     * @param description    a description of the document's content
     * @param content        optional text content of the document
     * @param type           the document type (POLICY or TRAINING)
     * @param completionType how completion is tracked (QUIZ, ACKNOWLEDGEMENT, or PRACTICAL)
     * @return the saved {@link TrainingDocument}
     * @throws Exception if the file cannot be stored
     */
    @PostMapping("/documents/upload")
    public TrainingDocument uploadDocument(
        @RequestParam("file") MultipartFile file,
        @RequestParam("title") String title,
        @RequestParam("description") String description,
        @RequestParam(value = "content", required = false) String content,
        @RequestParam("type") TrainingDocumentType type,
        @RequestParam("completionType") TrainingCompletionType completionType) throws Exception {
        return trainingService.uploadDocument(file, title, description, content, type, completionType);
    }

    /**
     * Downloads the file associated with a training document.
     * Serves the file inline with appropriate content type (PDF or plain text).
     *
     * @param id the ID of the training document
     * @return the file as a {@link Resource} with appropriate content headers
     * @throws Exception if the file cannot be found or read
     */
    @GetMapping("/documents/{id}/file")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws Exception {
        TrainingDocument doc = trainingService.getDocumentById(id);

        Path path = Paths.get(doc.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("File could not be read.");
        }

        String lowerName = doc.getFileName() != null ? doc.getFileName().toLowerCase() : "";

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (lowerName.endsWith(".pdf")) {
            mediaType = MediaType.APPLICATION_PDF;
        } else if (lowerName.endsWith(".txt")) {
            mediaType = MediaType.TEXT_PLAIN;
        }

        return ResponseEntity.ok()
            .contentType(mediaType)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + doc.getFileName() + "\"")
            .body(resource);
    }

    /**
     * Returns the quiz questions for a specific training document.
     *
     * @param documentId the ID of the training document
     * @return a list of {@link TrainingQuizQuestion} objects
     */
    @GetMapping("/{documentId}/quiz")
    public List<TrainingQuizQuestion> getQuizQuestions(@PathVariable Long documentId) {
        return trainingService.getQuizQuestions(documentId);
    }

    /**
     * Adds a new quiz question to a training document.
     * Intended for use by managers and administrators.
     *
     * @param documentId the ID of the training document to add the question to
     * @param request    the quiz question data including question text, options, and correct answer
     * @return the saved {@link TrainingQuizQuestion}
     */
    @PostMapping("/{documentId}/quiz/questions")
    public TrainingQuizQuestion addQuizQuestion(
        @PathVariable Long documentId,
        @RequestBody CreateQuizQuestionRequest request) {
        return trainingService.addQuizQuestion(documentId, request);
    }

    /**
     * Submits quiz answers for a training document and returns the result.
     *
     * @param documentId the ID of the training document being assessed
     * @param request    the submission containing the user's selected answers
     * @return a {@link TrainingQuizResultResponse} indicating pass/fail and score
     */
    @PostMapping("/{documentId}/quiz/submit")
    public TrainingQuizResultResponse submitQuiz(
        @PathVariable Long documentId,
        @RequestBody QuizSubmissionRequest request) {
        return trainingService.submitQuiz(documentId, request);
    }

    /**
     * Records that the currently authenticated user has acknowledged a training document.
     *
     * @param documentId the ID of the document to acknowledge
     */
    @PostMapping("/{documentId}/acknowledge")
    public void acknowledgeDocument(@PathVariable Long documentId) {
        trainingService.acknowledgeDocument(documentId);
    }

    /**
     * Records a practical training sign-off for a specific employee.
     * Intended for use by managers and administrators.
     *
     * @param documentId the ID of the training document the sign-off relates to
     * @param request    the sign-off data including the employee ID and optional notes
     */
    @PostMapping("/{documentId}/practical-signoff")
    public void approvePracticalTraining(
        @PathVariable Long documentId,
        @RequestBody PracticalSignOffRequest request) {
        trainingService.approvePracticalTraining(
            documentId,
            request.getEmployeeId(),
            request.getNote());
    }
}