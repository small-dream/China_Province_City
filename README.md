# China_Province_City

基于民政部行政区划页面抓取并整理省、市、区县三级数据，最终生成层级化 JSON 文件。

当前仓库中的程序入口是 `com.jx.MainGetData`，会从代码中写死的民政部页面读取数据，并在项目根目录生成：

- `2024年最新中华人民共和国县以上行政区划代码.json`

仓库中已经包含一份生成结果，可直接使用。

## 数据来源

当前代码使用的页面地址为：

- `https://www.mca.gov.cn/mzsj/xzqh/2025/202401xzqh.html`

程序通过 Jsoup 读取页面中这两个 class 对应的表格内容：

- `xl7021822`
- `xl7121822`

然后将页面中的行政区划代码与名称整理为结构化数据。

## 项目结构

核心文件如下：

- `src/main/java/com/jx/MainGetData.java`：抓取页面、解析列表、组装省市区层级、输出 JSON
- `src/main/java/com/jx/Province.java`：省级模型
- `src/main/java/com/jx/City.java`：市级模型
- `src/main/java/com/jx/Area.java`：区县模型
- `src/main/java/com/jx/JSONFormatUtils.java`：使用 Gson 美化写出 JSON
- `src/main/java/com/jx/FileUtils.java`：获取项目根目录
- `pom.xml`：Maven 依赖配置

## 数据结构

生成后的 JSON 结构如下：

```json
[
  {
    "code": "110000",
    "name": "北京市",
    "cityList": [
      {
        "code": "110000",
        "name": "北京市",
        "areaList": [
          {
            "code": "110101",
            "name": "东城区"
          }
        ]
      }
    ]
  }
]
```

字段说明：

- `Province.code` / `Province.name`：省级行政区代码与名称
- `Province.cityList`：该省下的城市列表
- `City.code` / `City.name`：市级行政区代码与名称
- `City.areaList`：该市下的区县列表
- `Area.code` / `Area.name`：区县级行政区代码与名称

## 当前代码的处理逻辑

`MainGetData.processData(...)` 现在采用按编码分层归类的方式处理数据：

1. 先扫描全部数据，找出所有以 `0000` 结尾的省级编码，建立 `provinceMap`
2. 对四个直辖市 `北京`、`上海`、`天津`、`重庆`，直接创建一个与省同名的城市节点
3. 再扫描全部数据，将以 `00` 结尾但不以 `0000` 结尾的编码识别为普通地级市，并挂到对应省份下
4. 对剩余编码：
   - 如果属于直辖市，则直接挂到该直辖市的城市节点下作为区县
   - 否则按前四位编码匹配所属地级市，挂到其 `areaList`
5. 对仍未归类的编码，按“特殊城市”处理，直接补挂到所属省份下

另外，当前代码还会先移除页面中的这条文本，避免干扰后续解析：

- `省直辖县级行政单位`

## 依赖版本

当前 `pom.xml` 中使用：

- Java 8
- `org.jsoup:jsoup:1.21.2`
- `com.google.code.gson:gson:2.13.2`

## 运行方式

### 方式一：在 IDE 中运行

直接运行：

- `src/main/java/com/jx/MainGetData.java`

执行完成后，会在项目根目录生成或覆盖：

- `2024年最新中华人民共和国县以上行政区划代码.json`

### 方式二：使用 Maven

项目是标准 Maven 结构，导入后可编译运行。若本机已安装 Maven，可自行通过 Maven 执行主类 `com.jx.MainGetData`。

## 注意事项

- 页面地址和 HTML class 名称目前是写死在代码里的；如果民政部页面结构调整，需要同步修改 `MainGetData.java`
- 当前输出文件名也是写死的，和抓取页面并不是自动联动的
- 特殊行政区划的归类依赖现有编码规则；如果上游页面格式变化，建议先检查生成结果
