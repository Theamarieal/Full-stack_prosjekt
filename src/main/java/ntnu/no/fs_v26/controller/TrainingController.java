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

@RestController
@RequestMapping("/api/v1/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @GetMapping("/policies")
    public List<TrainingDocument> getPolicies() {
        return trainingService.getDocumentsByType(TrainingDocumentType.POLICY);
    }

    @GetMapping("/materials")
    public List<TrainingDocument> getMaterials() {
        return trainingService.getDocumentsByType(TrainingDocumentType.TRAINING);
    }

    @GetMapping("/statuses/me")
    public List<TrainingStatusResponse> getMyTrainingStatuses() {
        return trainingService.getMyTrainingStatuses();
    }

    @GetMapping("/certifications/me")
    public List<Certification> getMyCertifications() {
        return trainingService.getMyCertifications();
    }

    @GetMapping("/certifications/team")
    public List<Certification> getTeamCertifications() {
        return trainingService.getOrganizationCertifications();
    }

    @GetMapping("/acknowledgements/me")
    public List<DocumentAcknowledgement> getMyAcknowledgements() {
        return trainingService.getMyAcknowledgements();
    }

    @GetMapping("/acknowledgements/team")
    public List<DocumentAcknowledgement> getTeamAcknowledgements() {
        return trainingService.getTeamAcknowledgements();
    }

    @GetMapping("/employees")
    public List<EmployeeOptionResponse> getOrganizationEmployees() {
        return trainingService.getOrganizationEmployees();
    }

    @PostMapping("/documents")
    public TrainingDocument createDocument(@RequestBody TrainingDocument document) {
        return trainingService.createDocument(document);
    }

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

    @GetMapping("/{documentId}/quiz")
    public List<TrainingQuizQuestion> getQuizQuestions(@PathVariable Long documentId) {
        return trainingService.getQuizQuestions(documentId);
    }

    @PostMapping("/{documentId}/quiz/questions")
    public TrainingQuizQuestion addQuizQuestion(
            @PathVariable Long documentId,
            @RequestBody CreateQuizQuestionRequest request) {
        return trainingService.addQuizQuestion(documentId, request);
    }

    @PostMapping("/{documentId}/quiz/submit")
    public TrainingQuizResultResponse submitQuiz(
            @PathVariable Long documentId,
            @RequestBody QuizSubmissionRequest request) {
        return trainingService.submitQuiz(documentId, request);
    }

    @PostMapping("/{documentId}/acknowledge")
    public void acknowledgeDocument(@PathVariable Long documentId) {
        trainingService.acknowledgeDocument(documentId);
    }

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