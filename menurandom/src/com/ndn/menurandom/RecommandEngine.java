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
		menuData.searchName = "수프";
		menuData.explanation = "추운날 따뜻한 감자수프 어떠세요?";
		
		return menuData;
	}
}
