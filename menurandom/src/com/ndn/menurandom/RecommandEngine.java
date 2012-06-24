package com.ndn.menurandom;

import com.ndn.menurandom.data.MenuData;

public class RecommandEngine {
	int _weather;
	
	public RecommandEngine(int weather) {
		_weather = weather;
	}
	
	public MenuData getRecommandMenuData() {
		MenuData menuData = new MenuData();
		menuData.name = "감자수프";
		menuData.imgName = "a001";
		menuData.explanation = "추운날 따뜻한 감자수프 어떠세요? 감자에 있는 성분이 몸을 따뜻하게 해준데요??";
		
		return menuData;
	}
}
