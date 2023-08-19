下载
---

[2022年最新中华人民共和国县以上行政区划代码.json](https://github.com/small-dream/China_Province_City/blob/master/2022%E5%B9%B4%E6%9C%80%E6%96%B0%E4%B8%AD%E5%8D%8E%E4%BA%BA%E6%B0%91%E5%85%B1%E5%92%8C%E5%9B%BD%E5%8E%BF%E4%BB%A5%E4%B8%8A%E8%A1%8C%E6%94%BF%E5%8C%BA%E5%88%92%E4%BB%A3%E7%A0%81.json.json)

[Github源码](https://github.com/small-dream/China_Province_City)

获取数据的来源
---------

什么地方可以获取最权威的省市县数据？当然是官网

[民政部门门户网站](http://www.mca.gov.cn/article/sj/)

在网站的最下面，你可以看到最新的行政区划分代码

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190312190020140.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2ppYW5neHVxYXo=,size_16,color_FFFFFF,t_70)

打开连接，数据是这样展示的：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190312190141809.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2ppYW5neHVxYXo=,size_16,color_FFFFFF,t_70)

显然，这样的数据我们是无法使用的，通过查看网页源码发现
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190312190234672.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2ppYW5neHVxYXo=,size_16,color_FFFFFF,t_70)
我们需要的地区名字和代码 都对应HTML 的 class 标签 xl7128029 和xl7028029 ，这样我们可以通过Jsoup 把这些数据读取出来
```
  try {
            //2019年11月中华人民共和国县以上行政区划代码网页
            Document doc = Jsoup.connect("http://www.mca.gov.cn/article/sj/xzqh/2019/2019/201912251506.html").maxBodySize(0).get();
            Elements elements = doc.getElementsByClass("xl7128029");
            //省和市
            Elements elementsProAndCity = doc.getElementsByClass("xl7028029");
            List<String> stringListProAndCity = elementsProAndCity.eachText();
            List<String> stringList = elements.eachText();
            List<String> stringName = new ArrayList<String>();
            List<String> stringCode = new ArrayList<String>();
            stringListProAndCity.addAll(stringList);
            for (int i = 0; i < stringListProAndCity.size(); i++) {
                if (i % 2 == 0) {
                    //地区代码
                    stringCode.add(stringListProAndCity.get(i));
                } else {
                    //地区名字
                    stringName.add(stringListProAndCity.get(i));
                }
            }
            //正常情况 两个 list size 应该 一样
            System.out.println("stringName  size= " + stringName.size() + "   stringCode   size= " + stringCode.size());
            if (stringName.size() != stringCode.size()) {
                throw new RuntimeException("数据错误");
            }
            List<Province> provinceList = processData(stringName, stringCode);
            String path = FileUtils.getProjectDir() + "/2019年11月中华人民共和国县以上行政区划代码" + ".json";
            JSONFormatUtils.jsonWriter(provinceList, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
```
分别建立 Province  City  Area 三个类用来 保存数据：

省：
```
/**
 * 省份
 * @author jx on 2018/4/12.
 */

class Province {
    private String code;
    private String name;
    private List<City> cityList;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<City> getCityList() {
        return cityList;
    }

    public void setCityList(List<City> cityList) {
        this.cityList = cityList;
    }
}
```
市：

```
/**
 * 地级市
 * @author jx on 2018/4/12.
 */

class City {
    private String code;
    private String name;
    private List<Area> areaList;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Area> getAreaList() {
        return areaList;
    }

    public void setAreaList(List<Area> areaList) {
        this.areaList = areaList;
    }
}

```

县、区：

```
/**
 * 区，县
 * @author jx on 2018/4/12.
 */

class Area {
    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

```

建立省市区对应关系，我们要判断这行数据对应的是省，市还是县，主要根据下面几个条件判断：

1、行政区划代码一共六位，前两位代表省，第三、四位代表市，第五六位代表县、区。
2、如果后四位为0，那么这一行为省。
3、如果只有后两位为0，那么为地级市
4、其他的为县
5、香港，台湾，澳门比较特殊，没有对应的市区，根据自己的需求选择性处理
6、新疆石河子市等比较特殊，不是以00结尾，需要单独处理

核心代码 ：

```
   private static List<Province> processData(List<String> stringName, List<String> stringCode) {
         List<Province> provinceList = new ArrayList<Province>();
 
 
         //获取省
         for (int i = 0; i < stringCode.size(); i++) {
             String provinceName = stringName.get(i);
             String provinceCode = stringCode.get(i);
             if (provinceCode.endsWith("0000")) {
                 Province province = new Province();
                 province.setCode(provinceCode);
                 province.setName(provinceName);
                 provinceList.add(province);
                 List<City> cities = new ArrayList<City>();
                 province.setCityList(cities);
             }
         }
 
 
         //获取市
         for (int i = 0; i < provinceList.size(); i++) {
             String provinceName = provinceList.get(i).getName();
             String provinceCode = provinceList.get(i).getCode();
             //直辖市 城市和省份名称一样
             if (provinceName.contains("北京") || provinceName.contains("上海") || provinceName.contains("天津") || provinceName.contains("重庆")) {
                 City city = new City();
                 List<Area> areas = new ArrayList<Area>();
                 city.setName(provinceName);
                 city.setCode(provinceCode);
                 city.setAreaList(areas);
                 provinceList.get(i).getCityList().add(city);
             } else {
                 for (int j = 0; j < stringCode.size(); j++) {
                     String cityName = stringName.get(j);
                     String cityCode = stringCode.get(j);
                     if (!cityCode.equals(provinceCode)) {
                         if (cityCode.startsWith(provinceCode.substring(0, 2))) {
                             if (cityCode.endsWith("00")) {
                                 City city = new City();
                                 List<Area> areas = new ArrayList<Area>();
                                 city.setName(cityName);
                                 city.setCode(cityCode);
                                 city.setAreaList(areas);
                                 provinceList.get(i).getCityList().add(city);
                             }
                         }
                     }
                 }
             }
         }
 
 
         //获取区县
         for (Province province : provinceList) {
             List<City> cities = province.getCityList();
             for (City city : cities) {
                 //遍历获取县区
                 String cityCode = city.getCode();
                 String cityName = city.getName();
                 for (int k = 0; k < stringCode.size(); k++) {
                     String areaName = stringName.get(k);
                     String areaCode = stringCode.get(k);
                     if (cityName.contains("北京") || cityName.contains("上海") || cityName.contains("天津") || cityName.contains("重庆")) {
                         if (!province.getCode().equals(areaCode) && areaCode.startsWith(province.getCode().substring(0, 2))) {
                             Area area = new Area();
                             area.setName(areaName);
                             area.setCode(areaCode);
                             city.getAreaList().add(area);
                         }
                     } else {
                         if (!areaCode.equals(cityCode) && areaCode.startsWith(cityCode.substring(0, 4))) {
                             Area area = new Area();
                             area.setName(areaName);
                             area.setCode(areaCode);
                             city.getAreaList().add(area);
                         }
                     }
 
                 }
 
             }
         }
 
 
         //已经处理的数据移除
         List<String> stringNameList = new ArrayList<>(stringName);
         List<String> stringCodeList = new ArrayList<>(stringCode);
         for (Province province : provinceList) {
             stringNameList.remove(province.getName());
             stringCodeList.remove(province.getCode());
             List<City> cities = province.getCityList();
             for (City city : cities) {
                 stringNameList.remove(city.getName());
                 stringCodeList.remove(city.getCode());
                 List<Area> listArea=city.getAreaList();
                 for (Area area:listArea){
                     stringNameList.remove(area.getName());
                     stringCodeList.remove(area.getCode());
                 }
             }
         }
 
         //处理石河子 特殊 市，City Code 不以00结尾
         for (Province province : provinceList) {
             for (int k = 0; k < stringCodeList.size(); k++) {
                 if (stringCodeList.get(k).startsWith(province.getCode().substring(0, 2))) {
                     City city = new City();
                     List<Area> areas = new ArrayList<Area>();
                     city.setName(stringCodeList.get(k));
                     city.setCode(stringNameList.get(k));
                     city.setAreaList(areas);
                     province.getCityList().add(city);
                 }
             }
         }
 
         return provinceList;
     }
```

最后在工程目录生成JSON文件：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190312190343932.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2ppYW5neHVxYXo=,size_16,color_FFFFFF,t_70)