import numpy as np, json
from PIL import Image
from scipy import ndimage as ndi
g=Image.open(r"C:\Users\OneProyect\Documents\TOKE-privado\ic_toke_logo_nuevo.webp").convert("RGBA")
g=g.crop(g.getchannel("A").getbbox())
S=600/max(g.size); g=g.resize((round(g.width*S),round(g.height*S)),Image.LANCZOS)
a=np.array(g.getchannel("A"),dtype=float)/255
a=ndi.gaussian_filter(a,0.4)
H,W=a.shape
lab,n=ndi.label(a>0.8)
def marching(f,level=0.5):
    # segmentos de marching squares con interpolación lineal; devuelve lazos cerrados
    segs={}
    def interp(p,q,vp,vq):
        t=(level-vp)/(vq-vp) if vq!=vp else .5
        return (p[0]+(q[0]-p[0])*t, p[1]+(q[1]-p[1])*t)
    h,w=f.shape
    edges=[]
    for y in range(h-1):
        for x in range(w-1):
            v=[f[y,x],f[y,x+1],f[y+1,x+1],f[y+1,x]]
            c=sum((1<<i) for i in range(4) if v[i]>=level)
            if c in(0,15): continue
            P=[(x,y),(x+1,y),(x+1,y+1),(x,y+1)]
            def e(i): j=(i+1)%4; return interp(P[i],P[j],v[i],v[j])
            tab={1:[(3,0)],2:[(0,1)],3:[(3,1)],4:[(1,2)],5:[(3,0),(1,2)],6:[(0,2)],7:[(3,2)],8:[(2,3)],9:[(0,2)],10:[(0,1),(2,3)],11:[(1,2)],12:[(1,3)],13:[(0,1)],14:[(3,0)]}
            for i,j in tab[c]: edges.append((e(i),e(j)))
    key=lambda p:(round(p[0],3),round(p[1],3))
    adj={}
    for idx,(p,q) in enumerate(edges):
        adj.setdefault(key(p),[]).append((idx,q)); adj.setdefault(key(q),[]).append((idx,p))
    used=set(); loops=[]
    for idx,(p,q) in enumerate(edges):
        if idx in used: continue
        used.add(idx); loop=[p]; cur=q
        while key(cur)!=key(loop[0]):
            loop.append(cur)
            nx=[(k,pt) for k,pt in adj.get(key(cur),[]) if k not in used]
            if not nx: break
            k,pt=nx[0]; used.add(k); cur=pt
        loops.append(loop)
    return loops
def dp(pts,eps):
    if len(pts)<3: return pts
    a,b=np.array(pts[0]),np.array(pts[-1]); d=b-a; nrm=np.hypot(*d)
    P=np.array(pts)
    dist=np.abs(d[0]*(P[:,1]-a[1])-d[1]*(P[:,0]-a[0]))/nrm if nrm>0 else np.hypot(*(P-a).T)
    i=int(np.argmax(dist))
    if dist[i]>eps: return dp(pts[:i+1],eps)[:-1]+dp(pts[i:],eps)
    return [pts[0],pts[-1]]
res=[]
for k in range(1,n+1):
    m=(lab==k)
    if m.sum()<200: continue
    mm=np.pad(ndi.binary_dilation(m,iterations=2),1)
    f=np.pad(a,1)*mm
    loops=marching(f)
    L=max(loops,key=len)
    L=[(x-1,y-1) for x,y in L]
    L=dp(L+[L[0]],0.12)[:-1]
    ys,xs=np.nonzero(m)
    res.append({"area":int(m.sum()),"cx":float(xs.mean()),"cy":float(ys.mean()),"bbox":[int(xs.min()),int(ys.min()),int(xs.max()),int(ys.max())],"pts":L})
res.sort(key=lambda r:-r["area"])
print(W,H,n,[(r["area"],len(r["pts"]),r["bbox"]) for r in res])
json.dump({"W":W,"H":H,"parts":res},open(".artifacts/contornos.json","w"))
