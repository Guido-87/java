@Component
public class GroqClient {

    private final WebClient webClient;

    public GroqClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + System.getenv("API_KEY"))
                .build();
    }

    public String chat(String prompt, String model) {
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(Map.of(
                        "model", model,
                        "messages", List.of(Map.of("role", "user", "content", prompt))
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}