package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.service.CandidateService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CandidateControllerTest {
    private CandidateService candidateService;
    private CandidateController candidateController;
    private MultipartFile testFile;

    @BeforeEach
    void initServices() {
        candidateService = mock(CandidateService.class);
        candidateController = new CandidateController(candidateService);
        testFile = new MockMultipartFile("testFile.img", new byte[]{1, 2, 3});
    }

    @Test
    void whenRequestCandidateListPageThenGetPageWithCandidates() {
        var candidate1 = new Candidate(1, "test1", "desc1", 1);
        var candidate2 = new Candidate(2, "test2", "desc2", 1);
        var expectedCandidates = List.of(candidate1, candidate2);
        when(candidateService.findAll()).thenReturn(expectedCandidates);

        var model = new ConcurrentModel();
        var view = candidateController.getAll(model);

        assertThat(view).isEqualTo("candidates/list");
        assertThat(model.getAttribute("candidates")).isSameAs(expectedCandidates);

    }

    @Test
    void whenRequestCandidateCreationPageThenReturnCreateView() {
        var view = candidateController.getCreationPage();

        assertThat(view).isEqualTo("candidates/create");
    }

    @Test
    void whenPostCandidateWithFileThenRedirectToCandidates() throws IOException {
        var candidate = new Candidate(1, "test1", "desc1", 1);
        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.save(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(candidate);

        var model = new ConcurrentModel();
        var view = candidateController.create(candidate, testFile, model);
        var actualCandidates = candidateArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidates).isSameAs(candidate);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    void whenCreateThrowsExceptionThenReturnErrorViewWithMessage() {
        var expectedException = new RuntimeException("Failed to write file");
        when(candidateService.save(any(Candidate.class), any(FileDto.class))).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = candidateController.create(new Candidate(), testFile, model);

        assertThat(view).isEqualTo("errors/candidates/404");
        assertThat(model.getAttribute("message")).isEqualTo(expectedException.getMessage());
    }

    @Test
    void whenCandidateExistsThenReturnOneViewWithCandidate() {
        var candidate = new Candidate(1, "test1", "desc1", 1);
        when(candidateService.findById(candidate.getId())).thenReturn(Optional.of(candidate));

        var model = new ConcurrentModel();
        var view = candidateController.getById(model, candidate.getId());

        assertThat(view).isEqualTo("candidates/one");
        assertThat(candidate).isSameAs(model.getAttribute("candidate"));
    }

    @Test
    void whenCandidateNotFoundThenReturnErrorView() {
        int id = 10;
        when(candidateService.findById(id)).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = candidateController.getById(model, id);

        assertThat(view).isEqualTo("errors/candidates/404");
        assertThat(model.getAttribute("message")).isEqualTo("Кандидат с указанным идентификатором не найден");
        assertThat(model.getAttribute("candidate")).isNull();
    }

    @Test
    void whenCandidateUpdatedThenRedirectToCandidates() throws IOException {
        var candidate = new Candidate(1, "test1", "desc1", 1);
        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);
        var actualCandidates = candidateArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(model.getAttribute("message")).isNull();
        assertThat(actualCandidates).isSameAs(candidate);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    void whenCandidateNotUpdatedThenReturnErrorView() {
        var candidate = new Candidate(1, "test1", "desc1", 1);
        when(candidateService.update(any(Candidate.class), any(FileDto.class))).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);

        assertThat(view).isEqualTo("errors/candidates/404");
        assertThat(model.getAttribute("message")).isEqualTo("Кандидат с указанным идентификатором не найден");
        assertThat(model.getAttribute("candidate")).isNull();
    }

    @Test
    void whenUpdateThrowsExceptionThenReturnErrorViewWithMessage() {
        var expectedException = new RuntimeException("Failed to write file");
        when(candidateService.update(any(Candidate.class), any(FileDto.class))).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = candidateController.update(new Candidate(), testFile, model);

        assertThat(view).isEqualTo("errors/candidates/404");
        assertThat(model.getAttribute("message")).isEqualTo(expectedException.getMessage());
        assertThat(model.getAttribute("candidate")).isNull();
    }

    @Test
    void whenCandidateDeletedThenRedirectToCandidates() {
        int id = 10;
        when(candidateService.deleteById(id)).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, id);

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(model.getAttribute("message")).isNull();
        assertThat(model.getAttribute("candidate")).isNull();
    }

    @Test
    void whenCandidateNotDeletedThenReturnErrorView() {
        int id = 10;
        when(candidateService.deleteById(id)).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, id);

        assertThat(view).isEqualTo("errors/candidates/404");
        assertThat(model.getAttribute("message")).isEqualTo("Кандидат с указанным идентификатором не найден");
        assertThat(model.getAttribute("candidate")).isNull();
    }
}