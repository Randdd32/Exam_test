import os

# Путь к папке src
SRC_DIR = r"C:\exam-test\exam-frontend\src"
# Выходной файл
OUTPUT_FILE = "project_listing.txt"

# Разрешённые расширения
EXTENSIONS = (".ts", ".tsx", ".css", ".module.css")

with open(OUTPUT_FILE, "w", encoding="utf-8") as out_file:
    for root, dirs, files in os.walk(SRC_DIR):
        for file in files:
            if file.endswith(EXTENSIONS):
                full_path = os.path.join(root, file)
                # Приводим путь к unix-стилю
                unix_path = full_path.replace("\\", "/")
                out_file.write(f'//"{unix_path}"\n')
                with open(full_path, "r", encoding="utf-8") as f:
                    out_file.write(f.read() + "\n\n")
                
print(f"Сборка завершена! Листинг сохранён в {OUTPUT_FILE}")