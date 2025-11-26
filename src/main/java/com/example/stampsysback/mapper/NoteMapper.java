package com.example.stampsysback.mapper;

import com.example.stampsysback.model.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoteMapper {
    // insert: generated key will be set to note.noteId
    int insert(Note note);

    // select non-hidden notes by room
    List<Note> selectByRoom(@Param("roomId") Integer roomId);

    // select all notes by room (include hidden)
    List<Note> selectByRoomIncludeHidden(@Param("roomId") Integer roomId);

    // select single note
    Note selectById(@Param("noteId") Integer noteId);

    // update hidden flag
    int updateHidden(@Param("noteId") Integer noteId, @Param("hidden") boolean hidden);
}