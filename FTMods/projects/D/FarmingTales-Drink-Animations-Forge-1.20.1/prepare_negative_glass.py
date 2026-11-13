"""Compatibility entry point: preserve v20 glass and the missing viewing-side repairs."""
import runpy
from pathlib import Path

ROOT=Path(__file__).absolute().parent
if __name__=='__main__':
    runpy.run_path(str(ROOT/'prepare_wine_surfaces.py'),run_name='__main__')
