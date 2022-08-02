#!/usr/bin/env python

import sys

#---
# Add items to dictionary
#---
def addToDict(vals, key, val):
    out = ''
    if key == 'MOL_ID':
        out = show(vals)    # Show old values
        vals.clear()        # Reset
        init = 1
    # Add key or append to previous key (line continuation)
    if key in vals:
        vals[key] += val
    else:
        vals[key] = val
    return out

#---
# Parse compound sub fields
#---
def parseKeyVal(keyPrev, line):
    field = line[10:].strip()
    if ':' in field:
        (key, val) = field.split(':', 1)
        key = key.strip()
    else:
        key = keyPrev
        val = field
    val = val.strip()
    if val.endswith(';'):
        val = val[:-1]
    return (key, val)

#---
# Show dict entries as one string
#---
def show(vals):
    ret = ''
    if 'MOL_ID' not in vals:
        return ret
    for key in ['MOL_ID', 'MOLECULE', 'SYNONYM', 'CHAIN']:
        if key in vals:
            ret += vals[key]
        ret += '\t'
    return ret[:-1]

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

# Parse name from command line
name = sys.argv[1]

# Initialize
done = False
vals = dict()
compounds = list()
orgs = list()
key = ''

for l in sys.stdin:
    l = l.rstrip()

    if l.startswith('COMPND'):
        done = True
        (key, val) = parseKeyVal(key, l)                # Parse compound sub fields
        outStr = addToDict(vals, key, val)
        if outStr: compounds.append(outStr)
    elif l.startswith('SOURCE'):
        (key, val) = parseKeyVal(key, l)                # Parse compound sub fields
        if key == 'ORGANISM_COMMON' or key == 'ORGANISM_SCIENTIFIC':
            orgs.append( val )
    else:
        if done: break;

# Add last element
compounds.append( show(vals) )

# Show results
out = [i for tup in zip(compounds, orgs) for i in tup]     # Merge lists and create string
out = '\t'.join( out )

count = len(compounds)
if count > 1:
    organisms = ','.join(orgs)
    print(f"{name}\t{organisms}\t{count}\t{out}")

