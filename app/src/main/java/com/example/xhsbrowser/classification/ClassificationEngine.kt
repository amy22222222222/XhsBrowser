package com.example.xhsbrowser.classification

class ClassificationEngine {

    private val categories = listOf(
        CategoryRule(1, "美妆护肤", listOf(
            "护肤", "化妆", "口红", "面膜", "粉底", "卸妆", "精华", "眼影", "腮红", "防晒",
            "隔离", "遮瑕", "BB霜", "CC霜", "水乳", "面霜", "洁面", "美妆", "彩妆", "底妆",
            "定妆", "眼线", "睫毛", "眉笔", "高光", "素颜", "痘痘", "祛痘", "黑头", "毛孔"
        )),
        CategoryRule(2, "穿搭时尚", listOf(
            "穿搭", "ootd", "OOTD", "衣服", "鞋子", "包包", "配饰", "帽子", "围巾", "首饰",
            "项链", "耳环", "戒指", "手表", "墨镜", "裙子", "裤子", "外套", "毛衣", "衬衫",
            "西装", "牛仔裤", "高跟鞋", "运动鞋", "帆布鞋", "靴子", "时尚", "潮流", "搭配"
        )),
        CategoryRule(3, "美食探店", listOf(
            "美食", "探店", "餐厅", "甜品", "火锅", "奶茶", "咖啡", "蛋糕", "面包", "烧烤",
            "日料", "韩料", "西餐", "中餐", "小吃", "零食", "早餐", "午餐", "晚餐", "宵夜",
            "深夜食堂", "网红店", "饮品", "冰淇淋", "巧克力", "好吃", "美食推荐"
        )),
        CategoryRule(4, "旅行攻略", listOf(
            "旅行", "旅游", "攻略", "景点", "酒店", "机票", "民宿", "自驾", "徒步", "签证",
            "护照", "出境", "国内游", "周末游", "长假", "露营", "日出", "海滩", "古镇", "爬山",
            "登山", "滑雪", "温泉", "度假", "打卡", "拍照圣地", "旅行vlog"
        )),
        CategoryRule(5, "家居好物", listOf(
            "家居", "收纳", "装修", "家具", "好物推荐", "神器", "种草", "好用", "推荐",
            "居家", "卧室", "客厅", "厨房", "卫生间", "阳台", "软装", "硬装", "改造",
            "整理", "断舍离", "极简", "租房改造", "家电", "清洁", "懒人", "实用"
        )),
        CategoryRule(6, "育儿母婴", listOf(
            "育儿", "宝宝", "母婴", "辅食", "产检", "孕妈", "孕妇", "待产", "月子",
            "产后", "母乳", "奶粉", "尿不湿", "婴儿", "幼儿", "早教", "亲子", "带娃",
            "宝宝穿搭", "宝宝辅食", "育儿经验", "二胎", "新生儿", "儿保"
        )),
        CategoryRule(7, "健身运动", listOf(
            "健身", "减肥", "瑜伽", "跑步", "运动", "增肌", "减脂", "塑形", "马甲线",
            "腹肌", "普拉提", "跳绳", "游泳", "骑行", "徒步", "撸铁", "健身房", "私教",
            "拉伸", "有氧", "无氧", "HIIT", "燃脂", "体脂", "卡路里", "饮食控制"
        )),
        CategoryRule(8, "数码科技", listOf(
            "手机", "电脑", "数码", "科技", "App", "软件", "硬件", "笔记本", "平板",
            "耳机", "音箱", "相机", "无人机", "智能家居", "游戏", "显卡", "CPU",
            "iPhone", "华为", "小米", "苹果", "安卓", "编程", "AI", "人工智能"
        )),
        CategoryRule(9, "学习教育", listOf(
            "学习", "考试", "考研", "英语", "读书", "考证", "考公", "考编", "法考",
            "CPA", "雅思", "托福", "留学", "论文", "PPT", "Excel", "笔记", "思维导图",
            "阅读", "书单", "自习", "图书馆", "网课", "学习方法", "记忆", "专注力"
        )),
        CategoryRule(10, "职场成长", listOf(
            "职场", "面试", "简历", "副业", "涨薪", "跳槽", "晋升", "管理", "创业",
            "兼职", "自媒体", "个人IP", "时间管理", "效率", "沟通", "社交", "人脉",
            "职场干货", "领导力", "谈判", "年终总结", "述职", "转行"
        )),
        CategoryRule(11, "情感心理", listOf(
            "情感", "恋爱", "分手", "心理", "情绪", "两性", "婚姻", "相亲", "失恋",
            "暗恋", "表白", "婆媳", "闺蜜", "友情", "家庭", "原生家庭", "成长", "内耗",
            "焦虑", "抑郁", "心理咨询", "讨好型", "边界感", "PUA", "自愈", "治愈"
        )),
        CategoryRule(12, "娱乐影视", listOf(
            "影视", "综艺", "追剧", "明星", "电影", "电视剧", "动漫", "音乐", "演唱会",
            "爱豆", "偶像", "男团", "女团", "选秀", "韩剧", "美剧", "日剧", "国剧",
            "纪录片", "豆瓣", "票房", "OST", "MV", "娱乐", "八卦", "瓜"
        )),
        CategoryRule(13, "萌宠动物", listOf(
            "猫", "狗", "宠物", "猫咪", "狗狗", "喵星人", "汪星人", "吸猫", "撸猫",
            "铲屎官", "金毛", "泰迪", "布偶", "英短", "美短", "柴犬", "哈士奇", "柯基",
            "仓鼠", "兔子", "鹦鹉", "鱼", "养宠", "领养", "萌宠", "可爱"
        ))
    )

    private val cache = mutableMapOf<String, Int>()

    fun classify(title: String, contentSnippet: String): Int {
        val key = "${title}|$contentSnippet"
        cache[key]?.let { return it }

        val text = "$title $contentSnippet"

        var bestCategory = 14 // default: 其他
        var bestScore = 0

        for (cat in categories) {
            var score = 0
            for (keyword in cat.keywords) {
                if (text.contains(keyword)) {
                    score += if (keyword.length >= 3) 3 else 1
                }
            }
            if (score > bestScore) {
                bestScore = score
                bestCategory = cat.id
            }
        }

        cache[key] = bestCategory
        return bestCategory
    }

    fun clearCache() {
        cache.clear()
    }

    fun getCategoryName(categoryId: Int): String {
        return categories.find { it.id == categoryId }?.name ?: "其他"
    }

    fun getCategoryColor(categoryId: Int): Int {
        return when (categoryId) {
            1 -> 0xFFFF6B6B.toInt()
            2 -> 0xFF4ECDC4.toInt()
            3 -> 0xFF45B7D1.toInt()
            4 -> 0xFF96CEB4.toInt()
            5 -> 0xFFFFEAA7.toInt()
            6 -> 0xFFDDA0DD.toInt()
            7 -> 0xFF98D8C8.toInt()
            8 -> 0xFFF7DC6F.toInt()
            9 -> 0xFFBB8FCE.toInt()
            10 -> 0xFF85C1E9.toInt()
            11 -> 0xFFF1948A.toInt()
            12 -> 0xFF82E0AA.toInt()
            13 -> 0xFFF8C471.toInt()
            else -> 0xFFBDBDBD.toInt()
        }
    }
}

private data class CategoryRule(
    val id: Int,
    val name: String,
    val keywords: List<String>
)
