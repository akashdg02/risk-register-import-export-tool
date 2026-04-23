// Logic for AI Developer 1 
public class AiServiceClient {
    private final RestTemplate restTemplate;

    public AiServiceClient(RestTemplateBuilder builder) {
        // Must include a 10s timeout 
        this.restTemplate = builder
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }

    public RiskDescriptionResponse getAiDescription(String title) {
        try {
            return restTemplate.postForObject("http://ai-service:5000/ai/describe", 
                                              new RiskTitleRequest(title), 
                                              RiskDescriptionResponse.class);
        } catch (Exception e) {
            return null; // Handle null gracefully in the service layer 
        }
    }
}