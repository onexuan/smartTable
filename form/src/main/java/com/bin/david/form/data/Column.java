package com.bin.david.form.data;

import android.util.Log;

import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.format.count.DecimalCountFormat;
import com.bin.david.form.data.format.count.ICountFormat;
import com.bin.david.form.data.format.count.NumberCountFormat;
import com.bin.david.form.data.format.count.StringCountFormat;
import com.bin.david.form.data.format.draw.IDrawFormat;
import com.bin.david.form.data.format.IFormat;
import com.bin.david.form.data.format.draw.TextDrawFormat;
import com.bin.david.form.listener.OnColumnItemClickListener;
import com.bin.david.form.utils.LetterUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by huang on 2017/10/31.
 */

public class Column<T> implements Comparable<Column> {

    /**
     * 列名
     */
    private String columnName;
    /**
     * 子类
     */
    private List<Column> children;


    private IFormat<T> format;
    private IDrawFormat<T> drawFormat;


    private String fieldName;

    private List<T> datas;

    private List<String> values;
    private boolean isFixed;
    private int maxValueLength = -1; //最长的长度
    private String longestValue = ""; //最长的值
    private int width;
    private int level;
    private Comparator<T> comparator;
    private ICountFormat<T,? extends Number> countFormat;
    private boolean isReverseSort;
    private OnColumnItemClickListener<T> onColumnItemClickListener;

    private boolean isAutoCount =false;
    private int id;

    private boolean isParent;

    public Column(String columnName, List<Column> children) {
        this.columnName = columnName;
        this.children = children;
        isParent = true;
    }

    public Column(String columnName, Column... children) {
        this(columnName, Arrays.asList(children));
    }

    public Column(String columnName, String fieldName) {
        this(columnName, fieldName, null, null);
    }

    public Column(String columnName, String fieldName, IFormat<T> format) {
        this(columnName, fieldName, format, null);
    }

    public Column(String columnName, String fieldName, IDrawFormat<T> format) {
        this(columnName, fieldName, null, format);
    }

    public Column(String columnName, String fieldName, IFormat<T> format, IDrawFormat<T> drawFormat) {
        this.columnName = columnName;
        this.format = format;
        this.fieldName = fieldName;
        //默认给一个TextDrawFormat
        this.drawFormat = (drawFormat == null ? new TextDrawFormat<T>() : drawFormat);
        datas = new ArrayList<>();
        values = new ArrayList<>();
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }


    public IFormat<T> getFormat() {
        return format;
    }

    public void setFormat(IFormat<T> format) {
        this.format = format;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setChildren(List<Column> children) {
        this.children = children;
    }

    public IDrawFormat<T> getDrawFormat() {
        return drawFormat;
    }

    public void setDrawFormat(IDrawFormat<T> drawFormat) {
        this.drawFormat = drawFormat;
    }

    public boolean isParent() {
        return isParent;
    }

    public void setParent(boolean parent) {
        isParent = parent;
    }

    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }



    /**
     * 获取数据
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public T getData(Object o) throws NoSuchFieldException, IllegalAccessException {
        Class clazz = o.getClass();
        String[] fieldNames = fieldName.split("\\.");
        String firstFieldName = fieldNames.length == 0 ? fieldName : fieldNames[0];
        Field field = clazz.getDeclaredField(firstFieldName);
        if (field != null) {
            Object child = o;
            if (fieldNames.length == 0 || fieldNames.length == 1) {
                return getFieldValue(field, o,true);
            }
            for (int i = 0; i < fieldNames.length; i++) {
                if (child == null) {
                    return null;
                }
                Class childClazz = child.getClass();
                Field childField = childClazz.getDeclaredField(fieldNames[i]);
                if (childField == null) {
                    return null;
                }
                if (i == fieldNames.length - 1) {
                    return getFieldValue(childField, child,true);
                } else {
                    field.setAccessible(true);
                    child = field.get(child);
                }
            }

        }
        return  null;
    }

        /**
         * 填充数据
         * @param objects 对象列表
         * @param tableInfo 表格信息
         * @param config 配置
         * @throws NoSuchFieldException
         * @throws IllegalAccessException
         */

    public void fillData(List<Object> objects, TableInfo tableInfo, TableConfig config) throws NoSuchFieldException, IllegalAccessException {
        if(countFormat != null){
            countFormat.clearCount();
        }
        if(datas.size() == objects.size()){
            return;
        }
        if (objects.size() > 0) {
            int[] lineHeightArray = tableInfo.getLineHeightArray();
            Object firstObject = objects.get(0);
            Class clazz = firstObject.getClass();
            String[] fieldNames = fieldName.split("\\.");
            String firstFieldName = fieldNames.length == 0 ? fieldName : fieldNames[0];
            Field field = clazz.getDeclaredField(firstFieldName);
            if (field != null) {
                int size = objects.size();
                for (int k = 0; k < size; k++) {
                    Object o = objects.get(k);
                    Object child = o;
                    if (o == null) {
                        addData(null,"",true);
                        setRowHeight(config, lineHeightArray, k,null);
                        continue;
                    }
                    if (fieldNames.length == 0 || fieldNames.length == 1) {
                        T t = getFieldValue(field, o,true);
                        setRowHeight(config, lineHeightArray, k,t);
                        continue;
                    }
                    for (int i = 0; i < fieldNames.length; i++) {
                        if (child == null) {
                            addData(null,"",true);
                            setRowHeight(config, lineHeightArray, k,null);
                            break;
                        }
                        Class childClazz = child.getClass();
                        Field childField = childClazz.getDeclaredField(fieldNames[i]);
                        if (childField == null) {
                            addData(null,"",true);
                            setRowHeight(config, lineHeightArray, k,null);
                            break;
                        }
                        if (i == fieldNames.length - 1) {
                            T t = getFieldValue(childField, child,true);
                            setRowHeight(config, lineHeightArray, k,t);
                        } else {
                            field.setAccessible(true);
                            child = field.get(child);
                        }
                    }

                }
            }
        }
    }

    /**
     * 设置每行的高度
     * 以及计算总数
     *
     * @param config          配置
     * @param lineHeightArray 储存高度数组
     * @param position        位置
     */
    private void setRowHeight(TableConfig config, int[] lineHeightArray, int position,T t) {
        if(t != null && isAutoCount && countFormat ==null){
            if(LetterUtils.isBasicType(t)){
                if(LetterUtils.isNumber(this)) {
                    countFormat = new NumberCountFormat<>();
                }else{
                    countFormat = new DecimalCountFormat<>();
                }
            }else{
                countFormat = new StringCountFormat<>(this);
            }
        }
        if(countFormat != null){
            countFormat.count(t);
        }
        int height = drawFormat.measureHeight(this, position, config)
                +2*config.getVerticalPadding();
        if (height > lineHeightArray[position]) {
            lineHeightArray[position] = height;
        }
    }

    /**
     * 填充数据
     * @param objects 对象列表
     * @param tableInfo 表格信息
     * @param config 配置
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */

    public void addData(List<Object> objects, TableInfo tableInfo, TableConfig config,int startPosition,boolean isFoot) throws NoSuchFieldException, IllegalAccessException {
        if(objects.size()+ startPosition == datas.size()){
            return;
        }
        if (objects.size() > 0) {
            int[] lineHeightArray = tableInfo.getLineHeightArray();
            Object firstObject = objects.get(0);
            Class clazz = firstObject.getClass();
            String[] fieldNames = fieldName.split("\\.");
            String firstFieldName = fieldNames.length == 0 ? fieldName : fieldNames[0];
            Field field = clazz.getDeclaredField(firstFieldName);
            if (field != null) {
                int size = objects.size();
                for (int k = 0; k < size; k++) {
                    Object o = objects.get(isFoot ? k:(size-1-k));
                    Object child = o;
                    if (o == null) {
                        addData(null,"",isFoot);
                        setRowHeight(config, lineHeightArray, k+startPosition,null);
                        continue;
                    }
                    if (fieldNames.length == 0 || fieldNames.length == 1) {
                        T t = getFieldValue(field, o,isFoot);
                        setRowHeight(config, lineHeightArray, k+startPosition,t);
                        continue;
                    }
                    for (int i = 0; i < fieldNames.length; i++) {
                        if (child == null) {
                            addData(null,"",isFoot);
                            setRowHeight(config, lineHeightArray, k+startPosition,null);
                            break;
                        }
                        Class childClazz = child.getClass();
                        Field childField = childClazz.getDeclaredField(fieldNames[i]);
                        if (childField == null) {
                            addData(null,"",isFoot);
                            setRowHeight(config, lineHeightArray, k+startPosition,null);
                            break;
                        }
                        if (i == fieldNames.length - 1) {
                            T t = getFieldValue(childField, child,isFoot);
                            setRowHeight(config, lineHeightArray, k+startPosition,t);
                        } else {
                            field.setAccessible(true);
                            child = field.get(child);
                        }
                    }

                }
            }
        }
    }

    /**
     * 动态添加数据
     * @param t 数据
     * @param value 值
     * @param isFoot 是否添加到尾部
     */
    private void addData(T t,String value,boolean isFoot){
        if(isFoot) {
            datas.add(t);
            values.add(value);
        }else {
            datas.add(0,t);
            values.add(0,value);
        }
    }


    /**
     * 反射得到值
     *
     * @param field 成员变量
     * @param o     对象
     * @throws IllegalAccessException
     */
    private T getFieldValue(Field field, Object o,boolean isFoot) throws IllegalAccessException {
        field.setAccessible(true);
        T t = (T) field.get(o);


        String value;
        if (format != null) {
            value = format.format(t);
        } else {
            value = t == null ? "" : t.toString();
        }
        if (value.length() > maxValueLength) {
            maxValueLength = value.length();
            longestValue = value;
        } addData(t,value,isFoot);
        return t;
    }

    /**
     * 获取等级 如果上面没有parent 则为1，否则等于parent 递归+1
     * @return
     */
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * 列的宽度
     * @return 宽度
     */
    public int getWidth() {
        return width;
    }
    /**
     * 设置列的宽度
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * 获取当前列最长值长度
     * @return 最长值长度
     */
    public int getMaxValueLength() {
        return maxValueLength;
    }

    /**
     * 获取当前列最长值
     * @return 最长值
     */
    public String getLongestValue() {
        return longestValue;
    }
    /**
     * 统计总数
     * @return 最长值
     */
    public  String getTotalNumString(){
        if(countFormat != null){
            return countFormat.getCountString();
        }
        return "";
    }
    /**
     * 获取子列列表
     */
    public List<Column> getChildren() {
        return children;
    }

    /**
     * 添加子列
     * @param column
     */
    public void addChildren(Column column) {
        children.add(column);
    }

    /**
     * 获取用于排序比较器
     * @return 排序比较器
     */
    public Comparator<T> getComparator() {
        return comparator;
    }
    /**
     * 设置用于排序比较器
     */
    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    /**
     * 获取统计格式化
     * @return 统计格式化
     */
    public ICountFormat<T, ? extends Number> getCountFormat() {
        return countFormat;
    }
    /**
     * 设置统计格式化
     */
    public void setCountFormat(ICountFormat<T, ? extends Number> countFormat) {
        this.countFormat = countFormat;
    }

    /**
     * 设置最长值
     * @param longestValue
     */
    public void setLongestValue(String longestValue) {
        this.longestValue = longestValue;
    }

    /**
     * 获取列ID
     * @return ID
     */
    public int getId() {
        return id;
    }
    /**
     * 设置列ID
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * 比较
     */
    @Override
    public int compareTo(Column o) {
        return  this.id - o.getId();
    }

    /**
     * 判断是否开启自动统计
     * @return 是否开启自动统计
     */
    public boolean isAutoCount() {
        return isAutoCount;
    }
    /**
     * 设置开启自动统计
     */
    public void setAutoCount(boolean autoCount) {
        isAutoCount = autoCount;
    }

    /**
     * 判断是否反序
     * @return 是否反序
     */
    public boolean isReverseSort() {
        return isReverseSort;
    }
    /**
     * 设置是否反序
     */
    public void setReverseSort(boolean reverseSort) {
        isReverseSort = reverseSort;
    }

    /**
     * 获取点击列监听
     * @return 点击列监听
     */
    public OnColumnItemClickListener<T> getOnColumnItemClickListener() {
        return onColumnItemClickListener;
    }
    /**
     * 设置点击列监听
     */
    public void setOnColumnItemClickListener(OnColumnItemClickListener<T> onColumnItemClickListener) {
        this.onColumnItemClickListener = onColumnItemClickListener;
    }


    /**
     * 判断是否固定
     * @return 是否固定
     */
    public boolean isFixed() {
        return isFixed;
    }

    /**
     * 设置是否固定
     */
    public void setFixed(boolean fixed) {
        isFixed = fixed;
    }


}
