package com.example.stampsysback.controller;

import com.example.stampsysback.dto.NoteRequest;
import com.example.stampsysback.dto.NoteResponse;
import com.example.stampsysback.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) { this.noteService = noteService; }

    @PostMapping("/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse createNote(@RequestBody NoteRequest req) {
        return noteService.createNote(req);
    }

    @GetMapping("/rooms/{roomId}/notes")
    public List<NoteResponse> listNotes(@PathVariable Integer roomId,
                                        @RequestParam(name = "includeHidden", defaultValue = "false") boolean includeHidden) {
        return noteService.listNotesByRoom(roomId, includeHidden);
    }

    @PatchMapping("/notes/{noteId}/hidden")
    public NoteResponse setHidden(@PathVariable Integer noteId,
                                  @RequestParam boolean hidden) {
        return noteService.setHidden(noteId, hidden);
    }
}