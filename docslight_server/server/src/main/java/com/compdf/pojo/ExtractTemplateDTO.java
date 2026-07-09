package com.compdf.pojo;

import com.compdf.entity.ExtractFieldPojo;
import com.compdf.utils.JsonUtils;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ComPDFKit-WPH 2025/10/23 星期四
 */
@Data
public class ExtractTemplateDTO {

    private Map<String, ExtractFieldPojo> keys;

    private List<Map<String, ExtractFieldPojo>> tableHeaders;

    private String name;

    private String id;

    private String fileId;

    /**
     * 模板状态：0 未启用，1 启用，2 删除
     */
    private Integer status;

    private Integer page;
    public List<String> toList(Map<String,?> map){
        return new ArrayList<>(map.keySet());
    }

    public List<String> toList(List<Map<String, ExtractFieldPojo>> list){
        ArrayList<String> keys = new ArrayList<>();
        list.forEach(listMap -> {
            listMap.forEach((key, value) -> {
                keys.add(key);
            });
        });
        return keys;
    }

//    public String keysToString(){
//        return this.toList(this.keys).toString();
//    }

//    public String tableHandlesToString(){
//        return this.toList(this.tableHandles).toString();
//    }

    public static void main(String[] args) {
        ExtractTemplateDTO extractTemplateDTO = new ExtractTemplateDTO();
        Map<String,String> m = new LinkedHashMap<>();
        m.put("Title", "");
        m.put("Order Date", "");
        m.put("Supplier info/Sold To", "");
        m.put("Order #", "");
        m.put("Buyer info/Ship To", "");
        m.put("Quote #", "");
        m.put("Delivery Date", "");
        m.put("Total", "");
//        extractTemplateDTO.setKeys(m);

        Map<String,String> mTable = new LinkedHashMap<>();
        mTable.put("Item details", "");
        mTable.put("Item / Description", "");
        mTable.put("Quantity", "");
        mTable.put("Unit Price", "");
        mTable.put("Subtotal", "");
//        extractTemplateDTO.setTableHandles(Collections.singletonList(mTable));
        extractTemplateDTO.setName("Order");
        System.out.println(JsonUtils.getJsonString(extractTemplateDTO));
    }

    @NotNull
    public static ExtractTemplateDTO getExtractTemplateDTO(FileParameterDTO fileParameter) {
        ExtractTemplateDTO extractTemplate = new ExtractTemplateDTO();
        List<String> keysDescribe = fileParameter.getKeysDescribe();
        List<String> tableHandlesDescribe = fileParameter.getTableHandlesDescribe();
        Map<String,String> keys = new LinkedHashMap<>();
        Map<String,String> tableHandles = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(keysDescribe)) {
            for (int i = 0; i < fileParameter.getKeys().size(); i++) {
                keys.put(fileParameter.getKeys().get(i), keysDescribe.get(i));
            }
        }else {
            for (int i = 0; i < fileParameter.getKeys().size(); i++) {
                keys.put(fileParameter.getKeys().get(i), "");
            }
        }
        if (!CollectionUtils.isEmpty(fileParameter.getTableHandlesDescribe())){
            for (int i = 0; i < fileParameter.getTableHandles().size(); i++) {
                tableHandles.put(fileParameter.getTableHandles().get(i), tableHandlesDescribe.get(i));
            }
        }else {
            for (int i = 0; i < fileParameter.getTableHandles().size(); i++) {
                tableHandles.put(fileParameter.getTableHandles().get(i), "");
            }
        }

//        extractTemplate.setKeys(keys);
//        extractTemplate.setTableHandles(Collections.singletonList(tableHandles));
        return extractTemplate;
    }
}
