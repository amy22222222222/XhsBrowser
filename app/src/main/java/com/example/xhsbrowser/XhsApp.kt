package com.example.xhsbrowser

import android.app.Application
import com.example.xhsbrowser.data.db.AppDatabase
import com.example.xhsbrowser.data.db.entity.Category
import com.example.xhsbrowser.data.repository.RecordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class XhsApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        appScope.launch {
            val db = AppDatabase.getInstance(this@XhsApp)
            val repo = RecordRepository(db.browsingRecordDao(), db.categoryDao())
            repo.initCategoriesIfNeeded(DEFAULT_CATEGORIES)
        }
    }

    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category(1, "美妆护肤", "护肤,化妆,口红,面膜,粉底,卸妆,精华,眼影,防晒", "#FF6B6B", 1),
            Category(2, "穿搭时尚", "穿搭,ootd,衣服,鞋子,包包,配饰,时尚", "#4ECDC4", 2),
            Category(3, "美食探店", "美食,探店,餐厅,甜品,火锅,奶茶,咖啡", "#45B7D1", 3),
            Category(4, "旅行攻略", "旅行,旅游,攻略,景点,酒店,机票,民宿", "#96CEB4", 4),
            Category(5, "家居好物", "家居,收纳,装修,家具,好物,种草,神器", "#FFEAA7", 5),
            Category(6, "育儿母婴", "育儿,宝宝,母婴,辅食,产检,孕妈", "#DDA0DD", 6),
            Category(7, "健身运动", "健身,减肥,瑜伽,跑步,运动,增肌", "#98D8C8", 7),
            Category(8, "数码科技", "手机,电脑,数码,科技,App,游戏", "#F7DC6F", 8),
            Category(9, "学习教育", "学习,考试,考研,英语,读书,考证", "#BB8FCE", 9),
            Category(10, "职场成长", "职场,面试,简历,副业,涨薪,创业", "#85C1E9", 10),
            Category(11, "情感心理", "情感,恋爱,分手,心理,情绪,婚姻", "#F1948A", 11),
            Category(12, "娱乐影视", "影视,综艺,追剧,明星,电影,动漫", "#82E0AA", 12),
            Category(13, "萌宠动物", "猫,狗,宠物,猫咪,狗狗,萌宠", "#F8C471", 13),
            Category(14, "其他", "", "#BDBDBD", 14)
        )
    }
}
