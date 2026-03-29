#!/usr/bin/env bash

# Raiz = directorio actual
BASE_DIR="."
OUTPUT_FILE="backend_dump.txt"

# Limpiar salida
> "$OUTPUT_FILE"

# Extensiones tipicas de Spring Boot + config
INCLUDE_EXTENSIONS=(
  "*.java" "*.kt"
  "*.xml"
  "*.yml" "*.yaml" "*.properties"
  "*.sql"
  "*.json"
  "*.md"
)

# Construir expresion find
FIND_EXPR=""
for ext in "${INCLUDE_EXTENSIONS[@]}"; do
  FIND_EXPR+=" -iname \"$ext\" -o"
done
FIND_EXPR=${FIND_EXPR::-3}

# Ejecutar find desde el directorio actual
eval "find \"$BASE_DIR\" -type f \( $FIND_EXPR \) \
  -not -path '*/.git/*' \
  -not -path '*/target/*' \
  -not -path '*/build/*' \
  -not -path '*/node_modules/*' \
  -not -path '*/.idea/*' \
  -not -path '*/.vscode/*'" | sort | while read -r file; do

  # Ruta relativa limpia (sin ./)
  relative_path="${file#./}"

  echo "### $relative_path" >> "$OUTPUT_FILE"
  echo "" >> "$OUTPUT_FILE"

  cat "$file" >> "$OUTPUT_FILE"

  echo -e "\n\n" >> "$OUTPUT_FILE"

done