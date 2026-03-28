package com.jx;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class MainGetData {

    private static final Set<String> DIRECT_CITIES = new HashSet<>(Arrays.asList("北京", "上海", "天津", "重庆"));

    public static void main(String[] args) {
        try {
            Document doc = Jsoup.connect("https://www.mca.gov.cn/mzsj/xzqh/2025/202401xzqh.html").maxBodySize(0).get();
            Elements elementsProAndCity = doc.getElementsByClass("xl7021822");
            Elements elements = doc.getElementsByClass("xl7121822");

            List<String> rawList = elementsProAndCity.eachText();
            rawList.addAll(elements.eachText());
            rawList.remove("省直辖县级行政单位");

            List<String> nameList = new ArrayList<>();
            List<String> codeList = new ArrayList<>();
            for (int i = 0; i < rawList.size(); i++) {
                if (i % 2 == 0) {
                    codeList.add(rawList.get(i));
                } else {
                    nameList.add(rawList.get(i));
                }
            }

            System.out.println("nameList.size=" + nameList.size() + "  codeList.size=" + codeList.size());
            if (nameList.size() != codeList.size()) {
                throw new RuntimeException("数据错误");
            }

            List<Province> provinceList = processData(nameList, codeList);
            String path = FileUtils.getProjectDir() + "/2024年最新中华人民共和国县以上行政区划代码.json";
            JSONFormatUtils.jsonWriter(provinceList, path);
        } catch (IOException e) {
            throw new RuntimeException("获取行政区划数据失败", e);
        }
    }

    private static List<Province> processData(List<String> nameList, List<String> codeList) {
        // 构建 code -> index 映射，避免重复遍历
        Map<String, Province> provinceMap = new LinkedHashMap<>();
        Map<String, City> cityMap = new LinkedHashMap<>();
        Set<String> processedCodes = new HashSet<>();

        // 一次遍历：建立省份索引
        for (int i = 0; i < codeList.size(); i++) {
            String code = codeList.get(i);
            String name = nameList.get(i);
            if (code.endsWith("0000")) {
                Province province = new Province();
                province.setCode(code);
                province.setName(name);
                province.setCityList(new ArrayList<City>());
                provinceMap.put(code, province);
                processedCodes.add(code);

                // 直辖市：直接创建同名城市
                if (isDirectCity(name)) {
                    City city = new City();
                    city.setCode(code);
                    city.setName(name);
                    city.setAreaList(new ArrayList<Area>());
                    province.getCityList().add(city);
                    cityMap.put(code, city);
                }
            }
        }

        // 一次遍历：建立城市索引（非直辖市）
        for (int i = 0; i < codeList.size(); i++) {
            String code = codeList.get(i);
            String name = nameList.get(i);
            if (!code.endsWith("0000") && code.endsWith("00")) {
                String provinceKey = code.substring(0, 2) + "0000";
                Province province = provinceMap.get(provinceKey);
                if (province != null && !isDirectCity(province.getName())) {
                    City city = new City();
                    city.setCode(code);
                    city.setName(name);
                    city.setAreaList(new ArrayList<Area>());
                    province.getCityList().add(city);
                    cityMap.put(code, city);
                    processedCodes.add(code);
                }
            }
        }

        // 一次遍历：分配区县
        for (int i = 0; i < codeList.size(); i++) {
            String code = codeList.get(i);
            String name = nameList.get(i);
            if (processedCodes.contains(code)) {
                continue;
            }
            String provinceKey = code.substring(0, 2) + "0000";
            Province province = provinceMap.get(provinceKey);
            if (province == null) {
                continue;
            }

            if (isDirectCity(province.getName())) {
                // 直辖市：区县直接挂在省级城市下
                City city = province.getCityList().get(0);
                Area area = new Area();
                area.setCode(code);
                area.setName(name);
                city.getAreaList().add(area);
                processedCodes.add(code);
            } else {
                String cityKey = code.substring(0, 4) + "00";
                City city = cityMap.get(cityKey);
                if (city != null) {
                    Area area = new Area();
                    area.setCode(code);
                    area.setName(name);
                    city.getAreaList().add(area);
                    processedCodes.add(code);
                }
            }
        }

        // 处理石河子等特殊市（Code 不以 00 结尾，未被归入任何城市）
        for (int i = 0; i < codeList.size(); i++) {
            String code = codeList.get(i);
            if (processedCodes.contains(code)) {
                continue;
            }
            String provinceKey = code.substring(0, 2) + "0000";
            Province province = provinceMap.get(provinceKey);
            if (province != null) {
                City city = new City();
                city.setCode(code);
                city.setName(nameList.get(i));
                city.setAreaList(new ArrayList<Area>());
                province.getCityList().add(city);
            }
        }

        return new ArrayList<>(provinceMap.values());
    }

    private static boolean isDirectCity(String name) {
        for (String keyword : DIRECT_CITIES) {
            if (name.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
