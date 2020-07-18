package com.starix.gdou.utils;

import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;

/**
 * @author Starix
 * @date 2020-07-18 16:31
 */
public class HtmlMailRenderUtil {

    public static String render(String templatePath, Kv data){
        Engine engine = Engine.use();
        engine.setDevMode(true);
        engine.setToClassPathSourceFactory();
        Template template = engine.getTemplate(templatePath);
        return template.renderToString(data);
    }

}
