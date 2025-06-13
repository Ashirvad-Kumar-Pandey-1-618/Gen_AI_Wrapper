# GenAI-1.0

This branch contains code for a basic integration of Gen AI with a Telegram ChatBot. Every 5 seconds, the application checks for new messages from Telegram and replies with an AI-generated response.

> **Note:** All APIs added to the `Controller` folder are only for testing purposes. In an ideal scenario, they wouldn't be required.

---

## How to Run

Follow these steps to set up and run the application:

1. **Clone the Repository**  
   https://github.com/Ashirvad-Kumar-Pandey-1-618/Gen_AI_Wrapper.git`

2. **Load the Gradle Project**

3. **Update Configuration**  
   Replace the placeholders `${AI_API}` and `${BOT_TOKEN}` in the `src/main/resources/application.properties` file with your actual API keys:
    - `GEMINI_API_KEY` for the AI API
    - `TELEGRAM_BOT_TOKEN` for the Telegram Bot

4. **Run the Application**  
   Execute the `main` method in the `GenAiApplication` class located at:  
   `src/main/java/com/example/GenAI/GenAiApplication.java`

5. **Interact with the Bot**  
   Open a Telegram chat with your bot using the following link:  
   [https://t.me/LIFC_Blr_Demo_bot](https://t.me/LIFC_Blr_Demo_bot)  
   Send a message to the bot and wait for its reply.

---

## Additional Tips

- Keep an eye on the console for any errors or logs during execution.
- Ensure your API keys and tokens are valid and have the necessary permissions.

---

## Dependencies

This project uses the following technologies and frameworks:
- **Java** (JDK 17 or higher recommended)
- **Spring Boot** (with Scheduling support)
- **Gradle** (for dependency management)

---

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.