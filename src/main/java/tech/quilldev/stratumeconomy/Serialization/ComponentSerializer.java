package tech.quilldev.stratumeconomy.Serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public class ComponentSerializer extends Serializer<Component> {
    @Override
    public void write(Kryo kryo, Output output, Component object) {
        final var textComponent = (TextComponent) object;
        final var content = textComponent.content();
        //Write the text from the string
        output.writeString(content);
        //Write the color
        final var color = textComponent.color();
        if (color == null) {
            //Write the RGB values of the component
            output.writeInt(255);
            output.writeInt(255);
            output.writeInt(255);
        } else {
            output.writeInt(color.red());
            output.writeInt(color.green());
            output.writeInt(color.blue());
        }
    }

    @Override
    public Component read(Kryo kryo, Input input, Class<? extends Component> type) {
        final var content = input.readString();
        final var r = input.readInt();
        final var g = input.readInt();
        final var b = input.readInt();
        return Component.text(content).color(TextColor.color(r, g, b));
    }
}
