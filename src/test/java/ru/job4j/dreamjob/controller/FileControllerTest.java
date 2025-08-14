package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.service.FileService;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

class FileControllerTest {

    private FileService fileService;
    private FileController fileController;
    private MultipartFile testFile;

    @BeforeEach
    void initServices() {
        fileService = Mockito.mock(FileService.class);
        fileController = new FileController(fileService);
        testFile = new MockMultipartFile("testFile.img", new byte[]{1, 2, 3});
    }

    @Test
    void whenFileExistsThenReturnOkWithContent() throws IOException {
        int id = 10;
        when(fileService.getFileById(id)).thenReturn(Optional.of(new FileDto(testFile.getOriginalFilename(), testFile.getBytes())));

        var response = fileController.getById(id);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testFile.getBytes());
    }

    @Test
    void whenFileNotFoundThenReturnNotFound() {
        int id = 10;
        when(fileService.getFileById(id)).thenReturn(Optional.empty());

        var response = fileController.getById(id);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }
}