import os

# Путь к корневой папке
root_dir = r"C:\exam-test\exam-backend\src\main\java\com\exam"

# Список для хранения содержимого всех .java файлов
java_files_content = []

# Проход по всем папкам и файлам
for dirpath, _, filenames in os.walk(root_dir):
    for filename in filenames:
        if filename.endswith(".java"):
            file_path = os.path.join(dirpath, filename)
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                java_files_content.append(content)

# Объединяем все тексты в один
all_java_text = "\n".join(java_files_content)

# Сохраняем результат в файл (по желанию)
with open("all_java_sources.txt", "w", encoding="utf-8") as output_file:
    output_file.write(all_java_text)

print("Готово. Код из всех .java файлов сохранён в all_java_sources.txt")