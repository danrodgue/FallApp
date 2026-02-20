#!/usr/bin/env python3
"""
Eliminar comentarios y colapsar saltos de línea excesivos en archivos bajo 07.datos/sh.
Procesa extensiones: .sh (preserva shebang), .sql, .py, .md, .txt
Crea copia de seguridad en 07.datos/sh/backups/YYYYMMDD_HHMMSS/

Uso: python remove_comments_and_collapse.py
"""
from pathlib import Path
import shutil
import datetime
import re
import tokenize
import io

BASE = Path(__file__).resolve().parent
BACKUPS = BASE / 'backups' / datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
BACKUPS.mkdir(parents=True, exist_ok=True)

processed = []

def remove_comments_sql(text: str) -> str:
    text = re.sub(r'/\*.*?\*/', '', text, flags=re.S)
    text = re.sub(r'(?m)^[ \t]*--.*$', '', text)
    text = re.sub(r'(?m)^[ \t]*#.*$', '', text)
    return text

def remove_comments_md(text: str) -> str:
    return re.sub(r'<!--.*?-->', '', text, flags=re.S)

def remove_comments_py(text: str) -> str:
    shebang = ''
    if text.startswith('#!'):
        first_line, sep, rest = text.partition('\n')
        shebang = first_line + '\n'
        text = rest
    try:
        tokens = list(tokenize.generate_tokens(io.StringIO(text).readline))
        filtered = [t for t in tokens if t.type != tokenize.COMMENT]
        new = tokenize.untokenize(filtered)
    except Exception:
        lines = text.splitlines()
        new_lines = [l for l in lines if not l.lstrip().startswith('#')]
        new = '\n'.join(new_lines) + ('\n' if text.endswith('\n') else '')
    return shebang + new

def remove_comments_sh(text: str) -> str:

    shebang = ''
    if text.startswith('#!'):
        first_line, sep, rest = text.partition('\n')
        shebang = first_line + '\n'
        text = rest

    lines = text.splitlines()
    new_lines = [l for l in lines if not l.lstrip().startswith('#')]
    new = '\n'.join(new_lines) + ('\n' if text.endswith('\n') else '')
    return shebang + new

def collapse_blank_lines(text: str) -> str:

    return re.sub(r'(?:\n[ \t\r\f]*){5,}', '\n\n', text)

EXTS = {'.sh', '.sql', '.py', '.md', '.markdown', '.txt'}

for p in sorted(BASE.iterdir()):
    if p.is_dir() and p.name == 'backups':
        continue
    if not p.is_file():
        continue
    if p.suffix.lower() not in EXTS:
        continue
    orig = p.read_text(encoding='utf-8')
    new = orig
    sfx = p.suffix.lower()
    if sfx == '.sql':
        new = remove_comments_sql(new)
    elif sfx in ('.md', '.markdown'):
        new = remove_comments_md(new)
    elif sfx == '.py':
        new = remove_comments_py(new)
    elif sfx == '.sh':
        new = remove_comments_sh(new)

    new = collapse_blank_lines(new)
    new = '\n'.join([line.rstrip() for line in new.splitlines()]) + ('\n' if new.endswith('\n') else '')

    if new != orig:
        bak = BACKUPS / (p.name + '.bak')
        shutil.copy2(p, bak)
        p.write_text(new, encoding='utf-8')
        processed.append(p.name)

print('Processed files:')
for f in processed:
    print(' -', f)
print('Backups saved to', BACKUPS)
