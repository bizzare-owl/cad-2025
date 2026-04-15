package ru.bsuedu.cad.lab;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;

@Primary
@Component
@RequiredArgsConstructor
public class HTMLTableRenderer implements Renderer {

    private final ProductProvider productProvider;

    @Override
    public void render() {
        try (FileWriter fw = new FileWriter("products.html")) {
            StringBuilder resultTable = new StringBuilder();
            resultTable.append(
                    """
                    <tr>
                            <th>product_id</th>
                            <th>name</th>
                            <th>description</th>
                            <th>category_id</th>
                            <th>price</th>
                            <th>stock_quantity</th>
                            <th>image_url</th>
                            <th>created_at</th>
                            <th>updated_at</th>
                    </tr>
                    """);
            productProvider.getProducts().forEach(product -> {
                resultTable.append("<tr>");
                resultTable.append("<td>").append(product.getProductId()).append("</td>");
                resultTable.append("<td>").append(product.getName()).append("</td>");
                resultTable.append("<td>").append(product.getDescription()).append("</td>");
                resultTable.append("<td>").append(product.getCategoryId()).append("</td>");
                resultTable.append("<td>").append(product.getPrice()).append("</td>");
                resultTable.append("<td>").append(product.getStockQuantity()).append("</td>");
                resultTable.append("<td>").append(product.getImageUrl()).append("</td>");
                resultTable.append("<td>").append(product.getCreatedAt()).append("</td>");
                resultTable.append("<td>").append(product.getUpdatedAt()).append("</td>");
                resultTable.append("</tr>");
            });
            fw.append("""
                    <html>
                    <head>
                    <meta charset="UTF-8">
                    <title>Table</title>
                    </head>
                    <body>
                    <table>
                    """ + resultTable +
                    """
                    </table>
                    </body>
                    </html>
                    """);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
