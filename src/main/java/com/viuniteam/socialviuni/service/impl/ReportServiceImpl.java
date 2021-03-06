package com.viuniteam.socialviuni.service.impl;

import com.viuniteam.socialviuni.dto.Profile;
import com.viuniteam.socialviuni.dto.request.report.ReportSaveRequest;
import com.viuniteam.socialviuni.dto.response.report.ReportResponse;
import com.viuniteam.socialviuni.dto.utils.comment.CommentResponseUtils;
import com.viuniteam.socialviuni.dto.utils.post.PostResponseUtils;
import com.viuniteam.socialviuni.dto.utils.share.ShareResponseUtils;
import com.viuniteam.socialviuni.dto.utils.user.UserAuthorResponseUtils;
import com.viuniteam.socialviuni.entity.*;
import com.viuniteam.socialviuni.enumtype.ReportStatusType;
import com.viuniteam.socialviuni.exception.BadRequestException;
import com.viuniteam.socialviuni.exception.OKException;
import com.viuniteam.socialviuni.exception.ObjectNotFoundException;
import com.viuniteam.socialviuni.repository.CommentRepository;
import com.viuniteam.socialviuni.repository.PostRepository;
import com.viuniteam.socialviuni.repository.ReportRepository;
import com.viuniteam.socialviuni.repository.ShareRepository;
import com.viuniteam.socialviuni.service.ReportService;
import com.viuniteam.socialviuni.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserService userService;
    private final Profile profile;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ShareRepository shareRepository;

    private final UserAuthorResponseUtils userAuthorResponseUtils;
    private final PostResponseUtils postResponseUtils;
    private final CommentResponseUtils commentResponseUtils;
    private final ShareResponseUtils shareResponseUtils;

    private void checkReportType(ReportSaveRequest reportSaveRequest){
        if(reportSaveRequest.getType()==null)
            throw new BadRequestException("Lo???i b??o c??o kh??ng ???????c ????? tr???ng");
    }

    @Override
    public void reportPost(ReportSaveRequest reportSaveRequest) {
        checkReportType(reportSaveRequest);
        Post post = postRepository.findOneById(reportSaveRequest.getPostId());
        if(post==null)
            throw new ObjectNotFoundException("B??i vi???t kh??ng t???n t???i");
        User userSource = userService.findOneById(profile.getId());
        if(userSource.getId().equals(post.getAuthor().getId()))
            throw new BadRequestException("Kh??ng th??? t??? b??o c??o ch??nh m??nh");
        if(reportRepository.existsByPostAndUserSource(post,userSource))
            throw new BadRequestException("Kh??ng th??? b??o c??o l???n 2");
        Report report = Report.builder()
                .userSource(userSource)
                .reportType(reportSaveRequest.getType())
                .post(post)
                .content(reportSaveRequest.getContent())
                .status(ReportStatusType.QUEUE)
                .build();
        reportRepository.save(report);
        throw new OKException("B??o c??o th??nh c??ng");
    }

    @Override
    public void reportComment(ReportSaveRequest reportSaveRequest) {
        checkReportType(reportSaveRequest);
        Comment comment = commentRepository.findOneById(reportSaveRequest.getCommentId());
        if(comment==null)
            throw new ObjectNotFoundException("Comment kh??ng t???n t???i");
        User userSource = userService.findOneById(profile.getId());
        if(userSource.getId().equals(comment.getUser().getId()))
            throw new BadRequestException("Kh??ng th??? t??? b??o c??o ch??nh m??nh");
        if(reportRepository.existsByCommentAndUserSource(comment,userSource))
            throw new BadRequestException("Kh??ng th??? b??o c??o l???n 2");
        Report report = Report.builder()
                .userSource(userSource)
                .reportType(reportSaveRequest.getType())
                .comment(comment)
                .content(reportSaveRequest.getContent())
                .status(ReportStatusType.QUEUE)
                .build();
        reportRepository.save(report);
        throw new OKException("B??o c??o th??nh c??ng");
    }

    @Override
    public void reportShare(ReportSaveRequest reportSaveRequest) {
        checkReportType(reportSaveRequest);
        Share share = shareRepository.findOneById(reportSaveRequest.getShareId());
        if(share==null)
            throw new ObjectNotFoundException("B??i chia s??? kh??ng t???n t???i");
        User userSource = userService.findOneById(profile.getId());
        if(userSource.getId().equals(share.getUser().getId()))
            throw new BadRequestException("Kh??ng th??? t??? b??o c??o ch??nh m??nh");
        if(reportRepository.existsByShareAndUserSource(share,userSource))
            throw new BadRequestException("Kh??ng th??? b??o c??o 2 l???n");
        Report report = Report.builder()
                .userSource(userSource)
                .reportType(reportSaveRequest.getType())
                .share(share)
                .content(reportSaveRequest.getContent())
                .status(ReportStatusType.QUEUE)
                .build();
        reportRepository.save(report);
        throw new OKException("B??o c??o th??nh c??ng");
    }

    @Override
    public void removeReport(Long reportId) {
        Report report = reportRepository.findOneById(reportId);
        if(report==null)
            throw new ObjectNotFoundException("B??o c??o kh??ng t???n t???i");
        if(userService.findOneById(profile.getId()).getId().equals(report.getUserSource().getId()) || userService.isAdmin(profile)){
            reportRepository.delete(report);
            throw new OKException("X??a b??o c??o th??nh c??ng");
        }
        throw new BadRequestException("Kh??ng c?? quy???n x??a b??o c??o");
    }
    @Override
    public Page<ReportResponse> getAllByUserSource(User user, Pageable pageable) {
        Page<Report> reportList = reportRepository.findAllByUserSourceOrderByIdDesc(user,pageable);
        List<ReportResponse> reportResponseList = new ArrayList<>();
        reportList.stream().forEach(
                report -> {
                    ReportResponse reportResponse = ReportResponse.builder()
                            .id(report.getId())
                            .userSource(userAuthorResponseUtils.convert(report.getUserSource()))
                            .status(report.getStatus())
                            .reportType(report.getReportType())
                            .createdDate(report.getCreatedDate())
                            .build();
                    if(report.getPost()!=null)
                        reportResponse.setPostResponse(postResponseUtils.convert(report.getPost()));
                    if(report.getComment()!=null)
                        reportResponse.setCommentResponse(commentResponseUtils.convert(report.getComment()));
                    if(report.getShare()!=null)
                        reportResponse.setShareResponse(shareResponseUtils.convert(report.getShare()));
                    reportResponseList.add(reportResponse);
                }
        );
        return new PageImpl<>(reportResponseList, pageable, reportResponseList.size());
    }
}
