省市区  JSON 下载  [点击这里](https://github.com/small-dream/China_Province_City/blob/master/province.json)

获取数据的来源
---------

什么地方可以获取最权威的省市县数据？当然是官网

[民政部门门户网站](http://www.mca.gov.cn/article/sj/)

在网站的最下面，你可以看到最新的行政区划分代码

![](http://wx1.sinaimg.cn/mw690/90bd89ffgy1fv6wluvsjej20m205qaaw.jpg)

打开连接，数据是这样展示的：

![](http://wx2.sinaimg.cn/mw690/90bd89ffgy1fv6wlvx3guj20kt0hhmxd.jpg)

显然，这样的数据我们是无法使用的

先复制网页内容到txt文件，然后删除无用的文字,最后得到这样的：
![](http://wx4.sinaimg.cn/mw690/90bd89ffgy1fv6wlvjqeqj20850gxdg1.jpg)



最后是输出结果：

![这里写图片描述](http://wx4.sinaimg.cn/mw690/90bd89ffgy1fv6wlwkybmj20ks0mk0ti.jpg)

代码生成JSON数据
----------

我们最终需要的是一份JSON数据，这样才能直接使用，接下来的工作需要通过读取文件，生成JSON数据。先读出文件的每一行，保存在List里面

```
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

读取每一行数据之后，我们要判断这行数据对应的是省，市还是县，主要根据下面几个条件判断：

1、行政区划代码一共六位，前两位代表省，第三、四位代表市，第五六位代表县、区。
2、如果后四位为0，那么这一行为省。
3、如果只有后两位为0，那么为地级市
4、其他的为县

核心代码 ：

```
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
```

