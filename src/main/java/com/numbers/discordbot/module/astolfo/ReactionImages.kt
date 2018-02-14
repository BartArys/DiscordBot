package com.numbers.discordbot.module.astolfo

import java.util.*

enum class ReactionImages(vararg val urls: String){

    GOODBYE("https://media.tenor.com/images/b80189eb8b01fafb812fde3df8cd2e51/tenor.gif"),
    LAUGH("https://media.tenor.com/images/70cc1d782efacc387351bc9de8ea9dfb/tenor.gif?itemid=10373500"),
    NEGATIVE(
            "https://media.tenor.com/images/f7d4427ccd1bc3dc2cb04c89db36fcf4/tenor.gif?itemid=9543016",
            "https://pa1.narvii.com/6533/c367747391ade0b731a1ac97833c10f2fda9b3de_hq.gif"
    ),
    EXCITED(
            "https://78.media.tumblr.com/00e387cc76907a8b176a0eb1e59915d3/tumblr_ot98cvtSNP1sc4nv9o3_400.gif",
            "http://cloud-3.steamusercontent.com/ugc/863989342438879931/8B65F7E1D96362445B4BB28EF7C34795B73F3D1D/",
            "http://pa1.narvii.com/6526/e70ef447802002a55f9923c195436557780e8bb0_hq.gif",
            "https://lh3.googleusercontent.com/-8q9cQPXqp3Y/WY7XGq9HIrI/AAAAAAAAFUw/LZ-gUyvxhLoASMw_MKoYS1chUqu9j1C4wCJoC/w540-h304/tumblr_otlkskEP731qa94xto1_540.gif",
            "https://68.media.tumblr.com/09278cb26446c9a7e2181fda57894856/tumblr_ovdbwn68841vysie1o1_400.gif"
    ),
    CHOOSE("https://media.tenor.com/images/f26653ef4154887745612d6fb9e834d7/tenor.gif?itemid=9542560"),
    INTRODUCTION(
            "https://78.media.tumblr.com/4bfd76011d92118c1ceada4f6fa9a2ef/tumblr_ousndbqdby1sebz5yo1_540.gif",
            "https://i.pinimg.com/originals/36/e6/71/36e671921eecfefde01cbfdd35c25997.gif",
            "https://pa1.narvii.com/5874/7dc8fd30286fe18f6ccaf5d0b2a59eeeb36a4c40_hq.gif",
            "https://s-media-cache-ak0.pinimg.com/originals/cd/fb/25/cdfb2522ad1d3b5d74a3105f1662b822.gif"
    ),
    DENIED(
            "https://i.imgur.com/WFwkFFv.gif",
            "https://avatars.mds.yandex.net/get-pdb/224463/715269a0-7dff-451f-a267-32f65c286547/orig"
    ),
    RESET("https://pa1.narvii.com/6605/9c0ff79e13a520d77bbd0b0e339ff29b117e816e_hq.gif"),
    ATTACK(
            "https://s-media-cache-ak0.pinimg.com/originals/da/68/68/da68685d05b3ca1dfafe8552697e1c92.gif",
            "https://68.media.tumblr.com/0a924a66ed0d942c2778a80dfabe66ec/tumblr_otoboqE1QM1u9f4wvo2_r1_540.gif",
            "https://static.tumblr.com/a3e0a4e28b86d56ed8c21adb88817e18/vekrs28/tmuos4xiy/tumblr_static_tumblr_static_filename_640.gif"
    ),
    SHRUG("http://cloud-3.steamusercontent.com/ugc/870744308638282986/1ED9693AE9300ED411B99C556C3D489EE43C8D72/"),
    TALKING_POSITIVE(
            "https://pa1.narvii.com/6521/b931a62d7cb3264a97a2d369bab30375265947eb_hq.gif",
            "https://avatars.mds.yandex.net/get-pdb/163339/d3fcb148-b9a8-417d-aba4-09ab0f3099a0/orig",
            "https://pa1.narvii.com/6526/b620869045e95452f3a9b92b6ac75e1e94fd456a_hq.gif",
            "https://pa1.narvii.com/6605/90887d671d946f99243fd55d9107b58e37b7e1c0_hq.gif"
    ),
    TALKING_EMBARRASSED(
            "http://cloud-3.steamusercontent.com/ugc/848221012957258190/49A52F9970EAD298BAA399BFDBE86500A4A203A6/",
            "https://i.imgur.com/2mwncSE.gif"
    ),
    MUSIC_START("https://vignette.wikia.nocookie.net/typemoon/images/c/c0/La_Black_Luna_Apocrypha.gif/revision/latest?cb=20170830161803"),
    PLAY_SONG("https://thumbs.gfycat.com/HorribleVigorousArmyant-small.gif"),
    SINISTER("https://78.media.tumblr.com/f67f26fe30ea833573b7b19d6985a315/tumblr_osf87dAj3Z1t0no49o1_500.gif"),
    ANNOYED(
            "https://s-media-cache-ak0.pinimg.com/originals/5d/8a/77/5d8a7722535231ad06a0ac95302c0e54.gif",
            "https://media1.tenor.com/images/d2939c100cf9da70928e8d8b8da46589/tenor.gif?itemid=10398164",
            "https://4.bp.blogspot.com/-W9YBccoDr3g/WflUgmZYdCI/AAAAAAAA-d0/eE51G2YRB0I0bZ6QuMZztCfesQmBIONogCKgBGAs/s1600/Omake%2BGif%2BAnime%2B-%2BFate%2BApocrypha%2B-%2BEpisode%2B17%2B-%2BAstolfo%2BKinda%2BBored.gif"
    ),
    FLATTERED("https://media1.tenor.com/images/e3246cbc1291608f0da972bb543de204/tenor.gif?itemid=9528041"),
    TRAP("https://memestatic2.fjcdn.com/thumbnails/comments/Made+it+into+a+gif+_59cd6dac41755452fa6f0587df120b2d.gif"),
    ANNOUNCE("https://avatars.mds.yandex.net/get-pdb/368827/affbda5a-c970-4f84-bc19-04d2083d94dc/orig")
}

fun<T> Array<T>.random(): T{
    return this[Random().nextInt(this.size)]
}
