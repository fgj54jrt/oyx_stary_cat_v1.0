package com.xgr.wonderful.utils;

import android.content.Context;

/**
 * 通过反射机制获取资源ID工具�?
 * @author lxh
 *
 */
public class MResource {
	
	/**
	 * 获取R.drawable.XXX 的id
	 * @param context
	 * @param name
	 * @return
	 */
   public static int getDrawableId(Context context,String name){
	 return getIdByName(context, "drawable", name);
   }
   /**
    * 获取R.layout.XXX �?id 
    * @param context
    * @param name
    * @return
    */
   public static int getLayoutId(Context context, String name){
	   return getIdByName(context, "layout", name);
   }
   
   /**
    * 获取R.raw.XXX 的id
    * @param context
    * @param name
    * @return
    */
   public static int getRawID(Context context, String name){
	   return getIdByName(context, "raw", name);
   }
   
   /**
    * 获取R.string.XXX的id
    * @param context
    * @param name
    * @return
    */
   
   public static int getStringId(Context context,String name){
	   return getIdByName(context, "string", name);
   }
   
   /**
    * 获取R.id.XXX的id
    * @param context
    * @param name
    * @return
    */
   
   public static int getID(Context context,String name){
	   return getIdByName(context, "id", name);
   }
   
   /**
    * 获取R.color.XXX 的id
    * @param context
    * @param name
    * @return
    */
   
   public static int getColorId(Context context, String name){
	   return getIdByName(context, "color", name);
   }
   
   /**
    * 获取R.anim.XXX的id
    * @param context
    * @param name
    * @return
    */
   
   public static int getAnimId(Context context, String name){
	   return getIdByName(context, "anim", name);
   }
   
   /**
    * 获取Styleable
    * @param context
    * @param name
    * @return
    */
   public static int[] getStyleable(Context context, String name) {
	   return getIdsByName(context, "styleable", name);
   }
   
   /**
    * 获取Styleable里的某个子项
    * @param context
    * @param name
    * @return
    */
   public static int getStyleableItem(Context context, String name) {
	   return getIdByName(context, "styleable", name);
   }
   
   /**
    * 获取R.xml.XXX的id
    * @param context
    * @param name
    * @return
    */
   public static int getXMLId(Context context,String name){
	   return getIdByName(context, "xml", name);
   }
   
   /**
    * 获取R.stylel.XXX的id
    * @param context
    * @param name
    * @return
    */
   public static int getStyleId(Context context,String name){
	   return getIdByName(context, "style", name);
   }
   
   /**
    * 获取R.array.XXX的id
    * @param context
    * @param name
    * @return
    */
   public static int getArrayId(Context context,String name){
	   return getIdByName(context, "array", name);
   }
	/**
	 * 根据资源名获取其ID
	 * @param context 
	 * @param className 资源类型名称	 
	 * @param name      资源ID
	 * @return
	 */
	private static  int getIdByName(Context context, String className, String name) {  
        String packageName = context.getPackageName();  
        Class<?> r = null;  
        int id = 0;  
        try {  
            r = Class.forName(packageName + ".R");  
  
            Class<?>[] classes = r.getClasses();  
            Class<?> desireClass = null;  
  
            for (int i = 0; i < classes.length; ++i) {  
                if (classes[i].getName().split("\\$")[1].equals(className)) {  
                    desireClass = classes[i];  
                    break;  
                }  
            }  
  
            if (desireClass != null)  
                id = desireClass.getField(name).getInt(desireClass);  
        } catch (ClassNotFoundException e) {  
            e.printStackTrace();  
        } catch (IllegalArgumentException e) {  
            e.printStackTrace();  
        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IllegalAccessException e) {  
            e.printStackTrace();  
        } catch (NoSuchFieldException e) {  
            e.printStackTrace();  
        }  
  
        return id;  
    }
	/**
	 * 根据资源名获取ID，返回数组
	 * @param context
	 * @param className
	 * @param name
	 * @return
	 */
	public static int[] getIdsByName(Context context, String className, String name) {  
	    String packageName = context.getPackageName();  
	    Class<?> r = null;  
	    int[] ids = null;  
	    try {  
	      r = Class.forName(packageName + ".R");  
	  
	      Class<?>[] classes = r.getClasses();  
	      Class<?> desireClass = null;  
	  
	      for (int i = 0; i < classes.length; ++i) {  
	        if (classes[i].getName().split("\\$")[1].equals(className)) {  
	          desireClass = classes[i];  
	          break;  
	        }  
	      }  
	  
	      if ((desireClass != null) && (desireClass.getField(name).get(desireClass) != null) && (desireClass.getField(name).get(desireClass).getClass().isArray()))  
	        ids = (int[])desireClass.getField(name).get(desireClass);  
	    }  
	    catch (ClassNotFoundException e) {  
	      e.printStackTrace();  
	    } catch (IllegalArgumentException e) {  
	      e.printStackTrace();  
	    } catch (SecurityException e) {  
	      e.printStackTrace();  
	    } catch (IllegalAccessException e) {  
	      e.printStackTrace();  
	    } catch (NoSuchFieldException e) {  
	      e.printStackTrace();  
	    }  
	  
	    return ids;  
	  }  
}
