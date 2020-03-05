package http.engine;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spark.ModelAndView;
import spark.TemplateEngine;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class FreeMarkerEngine extends TemplateEngine {

    private final Configuration configuration;

    private final BeansWrapper w = new BeansWrapperBuilder(Configuration.VERSION_2_3_21).build();
    private final TemplateHashModel statics = w.getStaticModels();

    public FreeMarkerEngine(Configuration configuration) {
        this.configuration = configuration;
    }

    public static Configuration createDefaultConfiguration() {
        Configuration configuration = new Configuration(new Version(2, 3, 23));
        configuration.setClassForTemplateLoading(FreeMarkerEngine.class, "/");
        return configuration;
    }

    public String render(@Nullable Map<String, Object> model, String page) {
        if (model == null) model = new HashMap<>();
        model.put("statics", statics);
        return render(new ModelAndView(model, page));
    }

    @Override
    public String render(@NotNull ModelAndView modelAndView) {
        try {
            StringWriter stringWriter = new StringWriter();
            Template template = configuration.getTemplate(modelAndView.getViewName());
            template.process(modelAndView.getModel(), stringWriter);
            return stringWriter.toString();
        } catch (IOException | TemplateException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
