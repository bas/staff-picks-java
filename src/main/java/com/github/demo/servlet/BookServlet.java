package com.github.demo.servlet;

import com.github.demo.model.Book;
import com.github.demo.service.BookService;
import com.github.demo.service.BookServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import com.launchdarkly.sdk.*;
import com.launchdarkly.sdk.server.*;

public class BookServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(BookServlet.class);

    private BookService bookService;

    private LDClient client;

    public BookServlet() throws Exception {
        logger.info("Starting Bookstore Servlet...");
        try {
            bookService = new BookService();
        } catch (BookServiceException e) {
            logger.error("Failed to instantiate BookService: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void init() throws ServletException {
        try {
            Properties launchDarklyProperties = new Properties();
            launchDarklyProperties.load(getClass().getResourceAsStream("/launchdarkly.properties"));
            String sdkKey = launchDarklyProperties.getProperty("SERVER_SIDE_SDK");

            client = new LDClient(sdkKey);

        } catch (IOException e) {
            logger.error("failed to initialize: " + e.getMessage());
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Properties versionProperties = new Properties();
        versionProperties.load(getClass().getResourceAsStream("/version.properties"));

        ServletContextTemplateResolver resolver = new ServletContextTemplateResolver(req.getServletContext());
        resolver.setPrefix("/");
        resolver.setSuffix(".html");

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        WebContext ctx = new WebContext(req, resp, getServletContext(), req.getLocale());
        ctx.setVariable("modified", Calendar.getInstance());
        ctx.setVariable("version", versionProperties.getProperty("version"));

        resp.setContentType("text/html; charset=UTF-8");

        LDContext context = LDContext.builder("context-key-123abc")
                .name("Sandy")
                .build();

        boolean showBookRating = client.boolVariation("show-book-rating", context, true);

        boolean showBanner = client.boolVariation("show-banner", context, true);

        String configureBanner = client.stringVariation("configure-banner", context, "Get 3 books for the price of 2");

        try {
            List<Book> books = bookService.getBooks();
            ctx.setVariable("books", books);

            ctx.setVariable("showBookRating", showBookRating);
            ctx.setVariable("showBanner", showBanner);
            ctx.setVariable("configureBanner", configureBanner);

            engine.process("books", ctx, resp.getWriter());
            
        } catch (BookServiceException e) {
            ctx.setVariable("error", e.getMessage());
            engine.process("error", ctx, resp.getWriter());
        }
    }

    @Override
    public void destroy() {
        try {
            client.close();
        } catch (IOException e) {
            logger.error("Failed to close client: ", e.getMessage());
        }
    }
}
