package com.example.stampsysback.service;

import com.example.stampsysback.dto.NoteRequest;
import com.example.stampsysback.dto.NoteResponse;
import com.example.stampsysback.dto.NoteCount;
import com.example.stampsysback.mapper.NoteMapper;
import com.example.stampsysback.model.Note;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteMapper noteMapper;

    public NoteService(NoteMapper noteMapper) {
        this.noteMapper = noteMapper;
    }

    @Transactional
    public NoteResponse createNote(NoteRequest req) {
        if (req.getNoteText() == null || req.getNoteText().trim().isEmpty()) {
            throw new IllegalArgumentException("noteText is required");
        }
        if (req.getRoomId() == null) {
            throw new IllegalArgumentException("roomId is required");
        }

        Note n = new Note();
        n.setNoteText(req.getNoteText());
        n.setRoomId(req.getRoomId());
        n.setHidden(false);
        // created_at は DB の DEFAULT now() に任せる（INSERT に含めない）

        noteMapper.insert(n); // generated key populated into n.noteId if supported

        Note saved = noteMapper.selectById(n.getNoteId());
        return toDto(saved);
    }

    public List<NoteResponse> listNotesByRoom(Integer roomId, boolean includeHidden) {
        List<Note> list = includeHidden ? noteMapper.selectByRoomIncludeHidden(roomId) : noteMapper.selectByRoom(roomId);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public NoteResponse setHidden(Integer noteId, boolean hidden) {
        int updated = noteMapper.updateHidden(noteId, hidden);
        if (updated == 0) {
            throw new IllegalArgumentException("note not found: " + noteId);
        }
        Note n = noteMapper.selectById(noteId);
        return toDto(n);
    }

    public List<NoteCount> getNoteCountsByClass(Integer classId){
        return noteMapper.selectNoteCountsByClass(classId);
    }

    private NoteResponse toDto(Note n) {
        if (n == null) return null;
        NoteResponse r = new NoteResponse();
        r.setNoteId(n.getNoteId());
        r.setNoteText(n.getNoteText());
        r.setRoomId(n.getRoomId());
        r.setHidden(n.getHidden());
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }
}