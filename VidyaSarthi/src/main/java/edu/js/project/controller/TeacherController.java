package edu.js.project.controller;

import edu.js.project.dto.*;
import edu.js.project.entity.News;
import edu.js.project.responseStructure.GetInfo;
import edu.js.project.service.FacultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/VidyaSarthi/faculty")
public class TeacherController {

    private final FacultyService service;

    @GetMapping("/checkStatus/{facultyId}")
    public ResponseEntity<?> checkProfileStatus(@PathVariable String facultyId){

        boolean profileStatus = service.checkFacultyStatus(facultyId);
        if(profileStatus){
            return ResponseEntity.ok("Profile Completed");
        }else {
            return ResponseEntity.ok("Profile Incomplete");
        }


    }

    @PostMapping("/uploadNews&Announcement")
    public ResponseEntity<?> uploadNews(@RequestBody News news){

        service.uploadNews(news);
        return ResponseEntity.ok("NewsUploaded");

    }

    @GetMapping("/getMaterialList")
    public ResponseEntity<?> materialList(){

        return ResponseEntity.ok(service.getMaterialList());

    }

    @GetMapping("/backend/getNotesList")
    public ResponseEntity<?> getUploadNotesOptions(){

        UploadNotesDto listSelection = service.getListSelection();
        return ResponseEntity.ok(listSelection);

    }

    @PostMapping("/uploadNotes")
    public ResponseEntity<?> uploadNotes(@RequestBody UploadNoteDto upload){

        service.uploadNotes(upload);
        return ResponseEntity.ok("Notes uploaded");

    }
    @PostMapping("/uploadPYQ")
    public ResponseEntity<?> uploadPYQ(@RequestBody UploadNoteDto upload){

        service.uploadPyq(upload);
        return ResponseEntity.ok("PYQ Uploaded");

    }

    @PostMapping("/uploadQB")
    public ResponseEntity<?> uploadQB(@RequestBody UploadNoteDto upload){

        service.uploadQb(upload);
        return ResponseEntity.ok("QB Uploaded");

    }

    @PostMapping("/editMaterial")
    public ResponseEntity<?> editMaterial(@RequestBody EditMaterialDto upload){

        service.editMaterial(upload);
        return ResponseEntity.ok("Material Edited Successfully");

    }

    @GetMapping("/complainList")
    public ResponseEntity<?>getComplainList(){

        List<ComplainDto> complainList = service.getComplainList();
        return ResponseEntity.ok(complainList);

    }

    @PostMapping("/getFacultyEmail")
    public ResponseEntity<?>getFacultyInfoUsingEmail(@RequestBody GetInfo getInfo){

        System.out.println(getInfo.getEmail()+"***********************");
        TeacherDto facultyInfoByEmail = service.getFacultyInfoByEmail(getInfo.getEmail());
        return ResponseEntity.ok(facultyInfoByEmail);

    }


}
