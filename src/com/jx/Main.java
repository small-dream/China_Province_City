package com.jx;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<List<String>> listList = new ArrayList<List<String>>();
        List<Province> provinceList = new ArrayList<Province>();
        File directory = new File("");// 参数为空
        String courseFile = null;
        try {
            courseFile = directory.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(courseFile);
        //每一行读成一个String
        List<String> strings = FileUtils.readFile(courseFile+"/province.txt");
        for (int i = 0; i < strings.size(); i++) {
            //每一行根据空格分割，便于取出有用的值
            List<String> list = Arrays.asList(strings.get(i).split(" "));
            listList.add(list);
        }
        for (int i = 0; i < listList.size(); i++) {
            if (listList.get(i).size() < 3) {
                continue;
            }
            String provinceName = listList.get(i).get(2);
            String provinceCode = listList.get(i).get(1);
            //遍历获取省级单位
            if (provinceCode.endsWith("0000")) {
                Province province = new Province();
                provinceList.add(province);
                province.setCode(provinceCode);
                province.setName(provinceName);
                List<City> cities = new ArrayList<City>();
                province.setCityList(cities);
                //香港，澳门，台湾，没有市级行政单位划分，城市 地区 和省份保持一致
                if (provinceName.contains("香港")||provinceName.contains("澳门")||provinceName.contains("台湾")){
                    City city = new City();
                    List<Area> areas = new ArrayList<Area>();
                    city.setName(provinceName);
                    city.setCode(provinceCode);
                    city.setAreaList(areas);
                    cities.add(city);
                    Area area = new Area();
                    area.setName(provinceName);
                    area.setCode(provinceCode);
                    areas.add(area);
                }
                //直辖市 城市和省份名称一样
                if (provinceName.contains("北京")||provinceName.contains("上海")||provinceName.contains("天津")||provinceName.contains("重庆")){
                    City city = new City();
                    List<Area> areas = new ArrayList<Area>();
                    city.setName(provinceName);
                    city.setCode(provinceCode);
                    city.setAreaList(areas);
                    cities.add(city);
                    //县区
                    for (int k = 0; k < listList.size(); k++) {
                        if (listList.get(k).size() < 3) {
                            continue;
                        }
                        String areaName = listList.get(k).get(2);
                        String areaCode = listList.get(k).get(1);
                        if (!provinceCode.equals(areaCode) && areaCode.startsWith(provinceCode.substring(0, 2))) {
                            Area area = new Area();
                            area.setName(areaName);
                            area.setCode(areaCode);
                            areas.add(area);
                        }
                    }
                }
                for (int j = 0; j < listList.size(); j++) {
                    if (listList.get(j).size() < 3) {
                        continue;
                    }
                    String cityName = listList.get(j).get(2);
                    String cityCode = listList.get(j).get(1);
                    //遍历获取地级市
                    if (!cityCode.equals(provinceCode) && cityCode.startsWith(provinceCode.substring(0, 2)) && cityCode.endsWith("00")) {
                        City city = new City();
                        List<Area> areas = new ArrayList<Area>();
                        city.setName(cityName);
                        city.setCode(cityCode);
                        city.setAreaList(areas);
                        cities.add(city);
                        //遍历获取县区
                        for (int k = 0; k < listList.size(); k++) {
                            if (listList.get(k).size() < 3) {
                                continue;
                            }
                            String areaName = listList.get(k).get(2);
                            String areaCode = listList.get(k).get(1);
                            if (!areaCode.equals(cityCode) && areaCode.startsWith(cityCode.substring(0, 4))) {
                                Area area = new Area();
                                area.setName(areaName);
                                area.setCode(areaCode);
                                areas.add(area);
                            }
                        }
                    }
                }
            }
        }
        //转化成JSON数据
        String jsonStrings = new Gson().toJson(provinceList);
        //写入文件
        FileUtils.createJsonFile(jsonStrings, courseFile+"/province.json");
    }

}
