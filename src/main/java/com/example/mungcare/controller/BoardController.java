package com.example.mungcare.controller;

import com.example.mungcare.dto.BoardDTO;
import com.example.mungcare.dto.PageRequestDTO;
import com.example.mungcare.dto.PageResultDTO;
import com.example.mungcare.entity.Board;
import com.example.mungcare.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;

    @PostMapping("/write") //글 작성
    public Integer boardRegister(BoardDTO boardDTO) {
        log.info("write...");
        Integer bNo = boardService.boardInput(boardDTO);
        System.out.println("id-----------"+bNo);
        return bNo;
    }

    @PostMapping("/list") //글 목록
    public PageResultDTO<BoardDTO, Board> boardList(PageRequestDTO pageRequestDTO) {
        log.info("list..."+pageRequestDTO);

        PageResultDTO<BoardDTO, Board> result = boardService.boardList(pageRequestDTO);
        System.out.println("-----------"+result);
        return result;
    }

    @PostMapping("/search") //카테고리 or 검색 별 목록
    public PageResultDTO<BoardDTO, Board> boardCatgory(PageRequestDTO pageRequestDTO) {
        log.info("boardCatgory..."+pageRequestDTO);

        PageResultDTO<BoardDTO, Board> result = boardService.boardCategoryList(pageRequestDTO);
        System.out.println("-----------"+result);
        return result;
    }

    @PostMapping("/detailView") //글 상세보기
    public BoardDTO boardDetail(@RequestParam("bNo")Integer bNo) {
        log.info("detailView...");
        log.info("--------------"+bNo);

        BoardDTO detail = boardService.read(bNo);
        System.out.println("---------------------------------------------------------"+detail);
        return detail;
    }

    @PostMapping("/remove") //글 삭제하기
    public boolean boardRemove(@RequestParam("bNo")Integer bNo) {
        log.info("remove...");
        boolean result = boardService.remove(bNo);
        return result;
    }

    @PostMapping("/modify") //글 수정하기
    public Integer boardModify(BoardDTO dto) {
        log.info("modify...");
        log.info("dto: "+dto);

        Integer result = boardService.modify(dto);

        return result;
    }

    @PostMapping("/count") //내가 작성한 게시글 수 확인하기
    public Integer boardCount(@RequestParam("id") String id) {
        log.info("count...");
        Integer count = boardService.boardCount(id);
        return count;
    }
}