package obvx.com.backend.service;

import obvx.com.backend.config.SupabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupabaseStorageServiceTest {

    @Mock
    private SupabaseConfig supabaseConfig;

    @Mock
    private RestTemplate restTemplate;

    private SupabaseStorageService supabaseStorageService;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        supabaseStorageService = new SupabaseStorageService(supabaseConfig, restTemplate);

        when(supabaseConfig.getUrl()).thenReturn("https://project.supabase.co");
        when(supabaseConfig.getBucket()).thenReturn("product-images");
        when(supabaseConfig.getKey()).thenReturn("supabase-anon-key");

        mockFile = new MockMultipartFile(
                "image",
                "photo.png",
                "image/png",
                "sample-image-bytes".getBytes()
        );
    }

    @Test
    @DisplayName("uploadImage - Succès lors de l'envoi de l'image et génération de l'URL publique")
    void uploadImage_success() throws IOException {
        ResponseEntity<String> successResponse = new ResponseEntity<>("Uploaded", HttpStatus.CREATED);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(successResponse);

        String resultUrl = supabaseStorageService.uploadImage(mockFile);

        assertThat(resultUrl).isNotNull();
        assertThat(resultUrl).startsWith("https://project.supabase.co/storage/v1/object/public/product-images/");
        assertThat(resultUrl).endsWith("-photo.png");
    }

    @Test
    @DisplayName("uploadImage - Lève RuntimeException quand Supabase renvoie un code non 2xx")
    void uploadImage_httpError_throwsRuntimeException() {
        ResponseEntity<String> errorResponse = new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(errorResponse);

        assertThatThrownBy(() -> supabaseStorageService.uploadImage(mockFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erreur lors de l'upload de l'image");
    }
}
