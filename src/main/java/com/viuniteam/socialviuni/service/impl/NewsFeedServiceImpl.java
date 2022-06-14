package com.viuniteam.socialviuni.service.impl;

import com.viuniteam.socialviuni.dto.Profile;
import com.viuniteam.socialviuni.dto.response.follow.FollowResponse;
import com.viuniteam.socialviuni.dto.response.friend.FriendResponse;
import com.viuniteam.socialviuni.dto.response.newsfeed.NewsFeedResponse;
import com.viuniteam.socialviuni.dto.response.post.PostResponse;
import com.viuniteam.socialviuni.dto.response.share.ShareResponse;
import com.viuniteam.socialviuni.dto.utils.post.PostResponseUtils;
import com.viuniteam.socialviuni.entity.mapper.UserFollowers;
import com.viuniteam.socialviuni.entity.mapper.UserFriends;
import com.viuniteam.socialviuni.repository.FollowerRepository;
import com.viuniteam.socialviuni.repository.FriendRepository;
import com.viuniteam.socialviuni.repository.PostRepository;
import com.viuniteam.socialviuni.service.*;
import com.viuniteam.socialviuni.utils.ListUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@AllArgsConstructor

public class NewsFeedServiceImpl implements NewsFeedService {
    private final Profile profile;
    private final FollowService followService;
    private final FriendService friendService;
    private final PostService postService;
    private final ShareService shareService;
    private final FriendRepository friendRepository;
    private final FollowerRepository followerRepository;
    private final PostRepository postRepository;
    private final PostResponseUtils postResponseUtils;
    private final Random random;

    private final int MAX_CODE = 2;
    private final int MIN_CODE = 1;
    @Override
    public List<NewsFeedResponse> getNewsFeed() {
        /*List<FriendResponse> friendResponseList = friendService.getAll(profile.getId());
        List<FollowResponse> followingList = followService.getAllFollowing(profile.getId());
        List<NewsFeedResponse> newsFeedResponseList = new ArrayList<>();

        int randomNews = renderRandom();

        if(randomNews==1){
            friendResponseList.stream().forEach(
                    friendResponse -> {
                        NewsFeedResponse newsFeedResponse = createNewsFeed(friendResponse.getUserInfoResponse().getId());
                        if(newsFeedResponse!=null)
                            newsFeedResponseList.add(newsFeedResponse);
                    }
            );
        }
        else{
            followingList.stream().forEach(
                    followResponse -> {
                        NewsFeedResponse newsFeedResponse = createNewsFeed(followResponse.getUserInfoResponse().getId());
                        if(newsFeedResponse!=null)
                            newsFeedResponseList.add(newsFeedResponse);
                    }
            );
        }

        return newsFeedResponseList;*/
        List<UserFriends> friendResponseList = friendRepository.findByUserOrderByIdDesc(profile.getId());
        List<UserFollowers> followingList = followerRepository.findByUserOrderByIdDesc(profile.getId());
        List<NewsFeedResponse> newsFeedResponseList = new ArrayList<>();

        int randomNews = renderRandom();

        if(randomNews==1){
            friendResponseList.stream().forEach(
                    friendResponse -> {
                        NewsFeedResponse newsFeedResponse = createNewsFeed(friendResponse.getUserId());
                        if(newsFeedResponse!=null)
                            newsFeedResponseList.add(newsFeedResponse);
                    }
            );
        }
        else{
            followingList.stream().forEach(
                    followResponse -> {
                        NewsFeedResponse newsFeedResponse = createNewsFeed(followResponse.getUserId());
                        if(newsFeedResponse!=null)
                            newsFeedResponseList.add(newsFeedResponse);
                    }
            );
        }
        if(newsFeedResponseList.size()==0){
            PostResponse postResponse = postResponseUtils.convert(ListUtils.getFirst(postRepository.findAll()));
            NewsFeedResponse newsFeedResponse = new NewsFeedResponse();
            newsFeedResponse.setPostResponse(postResponse);
            newsFeedResponseList.add(newsFeedResponse);
        }
        return newsFeedResponseList;
    }

    private NewsFeedResponse createNewsFeed(Long userId){
        int randomNews = renderRandom();
        if(randomNews==1){
            NewsFeedResponse newsFeedResponse = newPost(userId);
            if(newsFeedResponse!=null)
                return newsFeedResponse;
        }
        else {
            NewsFeedResponse newsFeedResponse = newShare(userId);
            if (newsFeedResponse != null)
                return newsFeedResponse;
        }
        return null;
    }


    private NewsFeedResponse newPost(Long userId){
        NewsFeedResponse newsFeedResponse = new NewsFeedResponse();
        PostResponse postResponse = postService.newPost(userId);
        if(postResponse!=null){
            newsFeedResponse.setPostResponse(postResponse);
            return newsFeedResponse;
        }
        return null;
    }
    private NewsFeedResponse newShare(Long userId){
        NewsFeedResponse newsFeedResponse = new NewsFeedResponse();
        ShareResponse shareResponse = shareService.newShare(userId);
        if (shareResponse != null){
            newsFeedResponse.setShareResponse(shareResponse);
            return newsFeedResponse;
        }
        return null;
    }

    private int renderRandom(){
        return random.nextInt((MAX_CODE - MIN_CODE) + 1) + MIN_CODE;
    }
}
