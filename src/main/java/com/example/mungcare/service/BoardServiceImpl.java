package com.example.mungcare.service;

import com.example.mungcare.dto.BoardDTO;
import com.example.mungcare.dto.PageRequestDTO;
import com.example.mungcare.dto.PageResultDTO;
import com.example.mungcare.entity.Board;
import com.example.mungcare.entity.QBoard;
import com.example.mungcare.repository.BoardRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
@Service
@RequiredArgsConstructor
@Log4j2
public class BoardServiceImpl implements BoardService{
    private final BoardRepository boardRepository;

    @Override
    public Integer boardInput(BoardDTO dto) { //글 작성
        try {
            validate(dtoToEntity(dto));
            log.info("boardInput-------------------");
            log.info(dto);
            Board board = dtoToEntity(dto);
            String text = dto.getBContent(); //bContent 내용 가져옴

            Document doc = Jsoup.parse(text); //Jsoup 라이브러리 활용하여

            //사진 추출하여 저장
            Elements imgs = doc.getElementsByTag("img"); //이미지 태그만 추출
            if(imgs.size()>0) {
                board.updatePhoto(imgs.get(0).attr("src")); //사진만 저장
            }

            //텍스트만 추출하여 저장
            String text2 = doc.text(); //html 태그 제외한 것만 추출
            board.updateText(text2); //텍스트만 저장
            
            boardRepository.save(board);
            System.out.println("------------------------"+board);
            return board.getBNo();
        } catch(Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

//    @Override
//    public PageResultDTO<BoardDTO, Object[]> boardList(PageRequestDTO pageRequestDTO) {
//        log.info("getList...");
//        Function<Object[], BoardDTO> fn = (en -> entityToDTO((Board)en[0], (Member)en[1]));
//        Page<Object[]> result = boardRepository.getBoardWithReplyCount(
//                pageRequestDTO.getPageable(Sort.by("bNo")
//        );
//        return new PageResultDTO<>(result, fn);
//    }

    @Override
    public PageResultDTO<BoardDTO, Board> boardList(PageRequestDTO pageRequestDTO) { //글 목록
        try{
            log.info("getList...");

            Pageable pageable = pageRequestDTO.getPageable(Sort.by("bNo").descending());

            Page<Board> result = boardRepository.findAll(pageable);

            //entityToDTO를 이용해서 Function을 생성하고 이를 PageResultTO로 구성한다.
            Function<Board, BoardDTO> fn = (entity -> entityToDTO(entity));
            //JPA의 처리 결과인 Page<Entity>와 Function을 전달해서, 엔티티 객체들을 DTO 리스트로 변환하고,
            //화면에 페이지 처리와 필요한 값들을 생성한다.

            return new PageResultDTO<>(result, fn);
            //PageResultDTO(dtoList=[BoardDTO(bNo=3, bContent=내용 변경, bTitle=제목 변경, bType=bTItle1, bViewCount=0, bLike=0, bReply=0, bCreateTime=2022-11-16T22:44:24.260285, id=B),
            // BoardDTO(bNo=1, bContent=bContent2, bTitle=bTItle2, bType=bTItle2, bViewCount=0, bLike=0, bReply=0, bCreateTime=2022-11-16T22:42:39.169953, id=Jj)],
            // totalPage=1, page=1, size=10, start=1, end=1, prev=false, next=false, pageList=[1])
        } catch(Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    @Override
    public PageResultDTO<BoardDTO, Board> boardCategoryList(PageRequestDTO pageRequestDTO) { //글 카테고리 목록
        try{
            log.info("boardCategoryList...");

            Pageable pageable = pageRequestDTO.getPageable(Sort.by("bNo").descending());

            BooleanBuilder booleanBuilder = getSearch(pageRequestDTO);


            Page<Board> result = boardRepository.findAll(booleanBuilder, pageable);

            Function<Board, BoardDTO> fn = (entity -> entityToDTO(entity));
            return new PageResultDTO<>(result, fn);
        } catch(Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    @Override
    public BoardDTO read(Integer bNo) { //글 상세보기
        updateView(bNo);

        //엔티티 객체를 가져왔다면, entityToDto()를 이용해 엔티티 객체를 DTO를 변환해서 반환
        Optional<Board> result = boardRepository.findById(bNo);
        //return result.get();
        System.out.println(entityToDTO(result.get()));
        return result.isPresent() ? entityToDTO(result.get()) : null;//isPresent(): null이 아닐 경우
    }

    @Override
    public void updateView(Integer bNo) { //조회수
        Board board = boardRepository.findById(bNo).get();
        board.updateViewCount(board.getBViewCount());
        boardRepository.save(board);
    }

    @Override
    @Transactional
    public boolean remove(Integer bNo) { //글 삭제
        try {
            boardRepository.deleteById(bNo);
            return true;
        } catch(Exception e) {
            log.info(e.getMessage());
            return false;
        }
    }

    @Override
    public Integer modify(BoardDTO dto) { //글 수정
        try {
            //수정 하는 항목: '제목', '내용', '카테고리'
            Optional<Board> result = boardRepository.findById(dto.getBNo());
            if(result.isPresent()) {
                Board board = result.get();
                board.changeTitle(dto.getBTitle()); //제목 수정
                board.changeContent(dto.getBContent()); //내용 수정
                String text = dto.getBContent(); //bContent 내용 가져옴

                Document doc = Jsoup.parse(text); //Jsoup 라이브러리 활용하여

                //사진 추출하여 저장
                Elements imgs = doc.getElementsByTag("img"); //이미지 태그만 추출
                if(imgs.size()>0) {
                    board.updatePhoto(imgs.get(0).attr("src"));  //사진만 저장
                }

                //텍스트만 추출하여 저장
                String text2 = doc.text(); //html 태그 제외한 것만 추출
                board.updateText(text2); //텍스트만 저장
                
                board.updateBType(dto.getBType()); //카테고리 수정
                boardRepository.save(board);
                return board.getBNo();
            }
            return null;
        } catch(Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    @Override
    public Integer boardCount(String id) { //내가 작성한 게시글 수
        try {
            List<Board> entity = boardRepository.findAll();
            Integer count = 0;

            for(Board board : entity) {
                if(id.equals(board.getId().getId()))
                    count += 1;
            }
            return count;
        } catch(Exception e) {
            log.info(e.getMessage());
            return null;
        }


    }


    /*
    @Override
    public List<BoardDTO> boardList() {
        List<Board> entity = boardRepository.findAll();

        List<BoardDTO> bList = new ArrayList<>();

        for (Board board : entity) {
            BoardDTO dto = BoardDTO.builder()
                    .bNo(board.getBNo())
                    .bContent(board.getBContent())
                    .bTitle(board.getBTitle())
                    .bType(board.getBType())
                    .bViewCount(board.getBViewCount())
                    .bLike(board.getBLike())
                    .bReply(board.getBReply())
                    .bCreateTime(board.getBCreateTime())
//                    .id(board.getId())
                    .build();

            bList.add(dto);
        }
        //만약 빈 리스트일 경우 []로 return
        return bList;
    }

     */

    private void validate(final Board board) {
        if(board == null) {
            log.warn("Entity cannot be null.");
            throw new RuntimeException("Entity cannot be null.");
        }
    }

    private BooleanBuilder getSearch(PageRequestDTO requestDTO) { //Querydsl 처리
        String type = requestDTO.getType();
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QBoard qBoard = QBoard.board;

        String keyword = requestDTO.getKeyword();

        BooleanExpression expression = qBoard.bNo.gt(0); //bNo >0 조건 생성

        booleanBuilder.and(expression);

        if(type == null || type.trim().length() == 0) { //검색 조건이 없는 경우
            return booleanBuilder;
        }

        //검색 조건 작성하기
        BooleanBuilder conditionBuilder = new BooleanBuilder();

        if(type.contains("type")) { //카테고리
            conditionBuilder.or(qBoard.bType.contains(keyword));
        }
        if(type.contains("title")) { //제목
            conditionBuilder.or(qBoard.bTitle.contains(keyword));
        }
        if(type.contains("content")) { //내용
            conditionBuilder.or(qBoard.bContent.contains(keyword));
        }

        //모든 조건 통합
        booleanBuilder.and(conditionBuilder);

        return booleanBuilder;

    }


}
