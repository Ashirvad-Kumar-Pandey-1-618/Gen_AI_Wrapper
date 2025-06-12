package com.example.GenAI.controller;
import com.example.GenAI.model.RequestModel;
import com.example.GenAI.model.ResponseModel;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EchoController {

    @PostMapping("/echo")
    @Operation(summary = "Simulate GenAI" , description = "This API Mocks GenAI and returns Modifies response")
    public ResponseModel echo(@RequestBody RequestModel request) {
        String inputText = request.getText();
        String outputText = "Mock GenAI Response :" + inputText + " - acknowledged";
        return new ResponseModel(outputText);
    }
}

