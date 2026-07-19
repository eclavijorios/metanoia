#!/usr/bin/env python3
"""Convert FBV USFX XML to nested JSON format.

USFX structure: verses are delimited by <v id="X" /> (start) and <ve /> (end)
markers within <p> or <q> elements. Words have Strong's numbers as <w s="H####">.
"""
import xml.etree.ElementTree as ET
import json
import re
import os

def extract_verse_text(verse_xml):
    """Extract readable text from a verse segment, stripping markup."""
    # Remove <f>...</f> footnotes, <w> tags (keeping content), <ve /> markers
    text = verse_xml
    text = re.sub(r'<f[^>]*>.*?</f>', '', text, flags=re.DOTALL)
    text = re.sub(r'<w[^>]*>(.*?)</w>', r'\1', text)
    text = re.sub(r'<ve\s*/?>', '', text)
    text = re.sub(r'<v[^>]*/>', '', text)
    text = re.sub(r'</?q[^>]*>', '', text)
    text = re.sub(r'</?p[^>]*>', '', text)
    text = re.sub(r'<[^>]+>', '', text)
    text = re.sub(r'\s+', ' ', text).strip()
    return text

def parse_book_text(book_elem, book_id):
    """Parse a book element and extract {chapter: {verse: text}}."""
    book_text = ET.tostring(book_elem, encoding="unicode")
    
    # Find all chapters
    chapters = {}
    current_ch = None
    current_v = None
    verse_texts = {}
    
    # We traverse the flattened text looking for <c>, <v>, <ve>
    lines = book_elem
    
    for child in book_elem:
        if child.tag == "c":
            ch_id = int(child.get("id", 1))
            current_ch = ch_id
            current_v = None
        elif child.tag in ("p", "q", "m", "b"):
            # Parse child's inner markup for verses
            child_xml = ET.tostring(child, encoding="unicode")
            # Find <v id="X" /> markers and corresponding <ve />
            # We'll split by <v and <ve
            parts = re.split(r'(<ve\s*/?>|<v\s+[^>]*/?>)', child_xml)
            current_verse_id = None
            verse_buf = ""
            
            for part in parts:
                if part.startswith("<v "):
                    # Save previous verse
                    if current_ch is not None and current_verse_id is not None:
                        text = extract_verse_text(verse_buf)
                        if text:
                            if current_ch not in chapters:
                                chapters[current_ch] = {}
                            chapters[current_ch][current_verse_id] = text
                    # Start new verse
                    vid = re.search(r'id="(\d+)"', part)
                    if vid:
                        current_verse_id = int(vid.group(1))
                    elif re.search(r'id=(\d+)', part):
                        vid = re.search(r'id=(\d+)', part)
                        if vid:
                            current_verse_id = int(vid.group(1))
                    verse_buf = ""
                elif part == "<ve />" or part == "<ve/>":
                    # End of verse
                    if current_ch is not None and current_verse_id is not None:
                        text = extract_verse_text(verse_buf)
                        if text:
                            if current_ch not in chapters:
                                chapters[current_ch] = {}
                            chapters[current_ch][current_verse_id] = text
                    current_verse_id = None
                    verse_buf = ""
                elif current_verse_id is not None:
                    verse_buf += part
    
    # Convert to list format
    chapters_list = []
    for ch_num in sorted(chapters.keys()):
        verses_dict = chapters[ch_num]
        verses_list = [verses_dict.get(v, "") for v in sorted(verses_dict.keys())]
        chapters_list.append({"verses": verses_list})
    
    return chapters_list

def convert_usfx_to_json(xml_path, output_path):
    tree = ET.parse(xml_path)
    root = tree.getroot()
    
    # Map USFX book IDs to OSIS IDs
    book_map = {
        "GEN": "Gen", "EXO": "Exod", "LEV": "Lev", "NUM": "Num", "DEU": "Deut",
        "JOS": "Josh", "JDG": "Judg", "RUT": "Ruth", "1SA": "1Sam", "2SA": "2Sam",
        "1KI": "1Kgs", "2KI": "2Kgs", "1CH": "1Chr", "2CH": "2Chr", "EZR": "Ezra",
        "NEH": "Neh", "EST": "Esth", "JOB": "Job", "PSA": "Ps", "PRO": "Prov",
        "ECC": "Eccl", "SNG": "Song", "ISA": "Isa", "JER": "Jer", "LAM": "Lam",
        "EZK": "Ezek", "DAN": "Dan", "HOS": "Hos", "JOL": "Joel", "AMO": "Amos",
        "OBA": "Obad", "JON": "Jonah", "MIC": "Mic", "NAM": "Nah", "HAB": "Hab",
        "ZEP": "Zeph", "HAG": "Hag", "ZEC": "Zech", "MAL": "Mal",
        "MAT": "Matt", "MRK": "Mark", "LUK": "Luke", "JHN": "John", "ACT": "Acts",
        "ROM": "Rom", "1CO": "1Cor", "2CO": "2Cor", "GAL": "Gal", "EPH": "Eph",
        "PHP": "Phil", "COL": "Col", "1TH": "1Thess", "2TH": "2Thess",
        "1TI": "1Tim", "2TI": "2Tim", "TIT": "Titus", "PHM": "Phlm",
        "HEB": "Heb", "JAS": "Jas", "1PE": "1Pet", "2PE": "2Pet",
        "1JN": "1John", "2JN": "2John", "3JN": "3John", "JUD": "Jude", "REV": "Rev"
    }

    output = []
    missing = set(book_map.keys())
    
    for book_elem in root.findall("book"):
        book_id = book_elem.get("id")
        if book_id not in book_map:
            continue
        missing.discard(book_id)
        osis_id = book_map[book_id]
        chapters = parse_book_text(book_elem, book_id)
        if not chapters:
            print(f"  WARNING: {book_id} ({osis_id}) has no chapters")
            continue
        output.append({"id": osis_id, "chapters": chapters})

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)
    
    total_verses = sum(
        len(ch["verses"]) for b in output for ch in b["chapters"]
    )
    
    print(f"Converted {len(output)} books -> {output_path}")
    print(f"File size: {os.path.getsize(output_path) / 1024:.0f} KB")
    print(f"Total verses: {total_verses}")
    if missing:
        print(f"Missing books: {missing}")

if __name__ == "__main__":
    convert_usfx_to_json("/tmp/fbv_ryano.xml",
                         "/Users/eclavijo/workspace/metanoia/backend/src/main/resources/data/fbv.json")
