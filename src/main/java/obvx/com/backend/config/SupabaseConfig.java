package obvx.com.backend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String url;

    @Value("${supabase.key}")
    private String key;

    @Value("${supabase.url}")
    private String bucket;

}
