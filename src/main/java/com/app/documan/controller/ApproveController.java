package com.app.documan.controller;

import com.app.documan.service.ApproveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approve")
@RequiredArgsConstructor
public class ApproveController {

    private final ApproveService approveService;

    @PostMapping
    @Operation(summary = "Approve all documents in the state 'NEW'",
            description = "Initiates the approval for all documents in the state 'NEW'. " +
                    "Approved documents are moved to the state 'APPROVED' and then archived to the predefined SMB share. " +
                    "Returns a count of documents sent for approval or BAD_REQUEST if no documents are found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approval process for all documents initiated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })    public ResponseEntity<String> approveAllDocs() {
        var cnt = approveService.sendAllToApprove();
        return cnt == 0 ? ResponseEntity.badRequest().build() : ResponseEntity.ok("Documents sent for approval: " + cnt);
    }

    @PostMapping("/{id}")
    @Operation(summary = "Approve a document by ID.",
            description = "Initiates the approval process for a document in the state 'NEW' by its unique ID. " +
                    "Approved document is moved to the state 'APPROVED' and then archived to the predefined SMB share." +
                    "Returns a confirmation message upon successful initiation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document approval process initiated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Document with specified ID not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })    public ResponseEntity<String> approveDocById(@PathVariable String id) {
        return ResponseEntity.status(approveService.sendToApprove(id)).build();
    }
}
