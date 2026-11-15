package com.courseerpbackend.javaqerar.controller;

import com.courseerpbackend.javaqerar.dto.ComparisonRequest;
import com.courseerpbackend.javaqerar.dto.ComparisonResponse;
import com.courseerpbackend.javaqerar.service.ComparisonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comparisons")
@RequiredArgsConstructor
public class ComparisonController {

    private final ComparisonService comparisonService;

    @PostMapping
    public ComparisonResponse compare(@Valid @RequestBody ComparisonRequest request) {
        return comparisonService.compare(request);
    }
}
