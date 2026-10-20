package dev.gaphunter.orders.api;

import dev.gaphunter.orders.domain.Order;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;

/**
 * Writes an {@link OrderExport} as CSV for {@code Accept: text/csv}. The finance team imports this file every night.
 */
@Component
public class OrderCsvMessageConverter extends AbstractHttpMessageConverter<OrderExport> {

    public static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    public OrderCsvMessageConverter() {
        super(TEXT_CSV);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return OrderExport.class.isAssignableFrom(clazz);
    }

    @Override
    protected OrderExport readInternal(Class<? extends OrderExport> clazz, HttpInputMessage inputMessage) {
        throw new HttpMessageNotReadableException("CSV input is not supported", inputMessage);
    }

    @Override
    protected void writeInternal(OrderExport export, HttpOutputMessage outputMessage) throws IOException {
        Writer writer = new OutputStreamWriter(outputMessage.getBody(), StandardCharsets.UTF_8);
        writer.write("id,customer_name,total,status\n");
        for (Order order : export.orders()) {
            writer.write(order.getId() + "," + quote(order.getCustomerName()) + "," + order.getTotal().toPlainString()
                    + "," + order.getStatus() + "\n");
        }
        writer.flush();
    }

    private static String quote(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
