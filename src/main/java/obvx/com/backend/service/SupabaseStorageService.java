package obvx.com.backend.service;

import obvx.com.backend.config.SupabaseConfig;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class SupabaseStorageService {

    private final SupabaseConfig supabaseConfig;
    private final RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Autowired
    public SupabaseStorageService(SupabaseConfig supabaseConfig) {
        this(supabaseConfig, new RestTemplate());
    }

    public SupabaseStorageService(SupabaseConfig supabaseConfig, RestTemplate restTemplate) {
        this.supabaseConfig = supabaseConfig;
        this.restTemplate = restTemplate;
    }

    public String uploadImage(MultipartFile file) throws IOException {

        String fileName = System.currentTimeMillis()
                + "-"
                + file.getOriginalFilename();

        String uploadUrl = supabaseConfig.getUrl()
                + "/storage/v1/object/"
                + supabaseConfig.getBucket()
                + "/"
                + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseConfig.getKey());
        headers.set("apikey", supabaseConfig.getKey());
        headers.setContentType(
                MediaType.parseMediaType(file.getContentType())
        );

        HttpEntity<ByteArrayResource> request = new HttpEntity<>(
                new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return fileName;
                    }
                },
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Erreur lors de l'upload de l'image");
        }

        return supabaseConfig.getUrl()
                + "/storage/v1/object/public/"
                + supabaseConfig.getBucket()
                + "/"
                + fileName;
    }
}
