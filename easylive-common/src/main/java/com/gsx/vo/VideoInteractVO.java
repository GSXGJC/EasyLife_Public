package com.gsx.vo;

/**
 * 视频互动状态返回体。
 *
 * 一个对象同时回答两个问题：
 * 1. "这个视频的计数"——likeCount/coinCount/collectCount（和 VideoVO 里的计数同源，都是 video_info 行）
 * 2. "当前登录用户对这个视频做了什么"——liked/collected/coined/myCoinCount
 *    + 我的硬币余额 currentCoinCount（投币面板要展示"我还有几个币"）
 *
 * 点赞/收藏/投币等操作接口成功后也返回它，前端拿到后不用再请求一次详情，
 * 直接用它刷新按钮状态和计数即可。
 */
public class VideoInteractVO {

    /** 当前用户是否已点赞该视频 */
    private Boolean liked;
    /** 当前用户是否已收藏该视频 */
    private Boolean collected;
    /** 当前用户是否已给该视频投过币 */
    private Boolean coined;
    /** 我给这个视频投了几个币（没投过是 0；投过是 1 或 2） */
    private Integer myCoinCount;

    /** 视频总点赞数 */
    private Integer likeCount;
    /** 视频总投币数 */
    private Integer coinCount;
    /** 视频总收藏数 */
    private Integer collectCount;

    /** 我的硬币余额（未登录时是 0） */
    private Integer currentCoinCount;

    public Boolean getLiked() { return liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }

    public Boolean getCollected() { return collected; }
    public void setCollected(Boolean collected) { this.collected = collected; }

    public Boolean getCoined() { return coined; }
    public void setCoined(Boolean coined) { this.coined = coined; }

    public Integer getMyCoinCount() { return myCoinCount; }
    public void setMyCoinCount(Integer myCoinCount) { this.myCoinCount = myCoinCount; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getCoinCount() { return coinCount; }
    public void setCoinCount(Integer coinCount) { this.coinCount = coinCount; }

    public Integer getCollectCount() { return collectCount; }
    public void setCollectCount(Integer collectCount) { this.collectCount = collectCount; }

    public Integer getCurrentCoinCount() { return currentCoinCount; }
    public void setCurrentCoinCount(Integer currentCoinCount) { this.currentCoinCount = currentCoinCount; }
}
