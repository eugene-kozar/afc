package com.example.filemanager;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyArrayAdapter extends ArrayAdapter<String> {
	private final Activity context;
	private final List<String> names;

	public MyArrayAdapter(Activity context, List<String> names) {
		super(context, R.layout.row, names);
		this.context = context;
		this.names = names;
	}

	// Класс для сохранения во внешний класс и для ограничения доступа
	// из потомков класса
	static class ViewHolder {
		public ImageView imageView;
		public TextView textView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ViewHolder буферизирует оценку различных полей шаблона элемента

		ViewHolder holder;
		// Очищает сущетсвующий шаблон, если параметр задан
		// Работает только если базовый шаблон для всех классов один и тот же
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.row, null, true);
			holder = new ViewHolder();
			holder.textView = (TextView) rowView.findViewById(R.id.label);
			holder.imageView = (ImageView) rowView.findViewById(R.id.icon);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}

		holder.textView.setText(names.get(position));
		// Изменение иконки для папок и файлов
		String s = names.get(position);
		if (s.equals("external_sd")) {
			holder.imageView.setImageResource(R.drawable.image_sd);
		} else if (new File (MainActivity.currentDirectory.toString() + "/" + s).isDirectory()) {
			holder.imageView.setImageResource(R.drawable.img_folder);
		} else if (s.endsWith(".afc")){
			holder.imageView.setImageResource(R.drawable.img_lock);
		} else {
			holder.imageView.setImageResource(R.drawable.img_file);
		}

		return rowView;
	}
}
