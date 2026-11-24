import { createContext, useContext, useEffect, useState } from 'react'

type Role = 'mozo' | 'cocinero' | 'almacenero'
type DemoState = 'contenido' | 'cargando' | 'vacio' | 'error'
type ThemeMode = 'claro' | 'oscuro' | 'automatico'
type IconName =
  | 'arrow'
  | 'bell'
  | 'box'
  | 'check'
  | 'chevron'
  | 'clock'
  | 'close'
  | 'cutlery'
  | 'grid'
  | 'history'
  | 'home'
  | 'minus'
  | 'plus'
  | 'search'
  | 'user'
  | 'warning'

const iconPaths: Record<IconName, React.ReactNode> = {
  arrow: <><path d="M5 12h14M13 6l6 6-6 6" /></>,
  bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" /><path d="M10 21h4" /></>,
  box: <><path d="m4 7 8-4 8 4v10l-8 4-8-4Z" /><path d="m4 7 8 4 8-4M12 11v10" /></>,
  check: <path d="m5 12 4 4L19 6" />,
  chevron: <path d="m9 6 6 6-6 6" />,
  clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></>,
  close: <><path d="m6 6 12 12M18 6 6 18" /></>,
  cutlery: <><path d="M7 3v7M4 3v5a3 3 0 0 0 6 0V3M7 11v10M17 3v18M17 3c-3 3-3 8 0 10" /></>,
  grid: <><rect x="3" y="3" width="7" height="7" rx="2" /><rect x="14" y="3" width="7" height="7" rx="2" /><rect x="3" y="14" width="7" height="7" rx="2" /><rect x="14" y="14" width="7" height="7" rx="2" /></>,
  history: <><path d="M3 12a9 9 0 1 0 3-6.7L3 8" /><path d="M3 3v5h5M12 7v5l3 2" /></>,
  home: <><path d="m3 11 9-8 9 8" /><path d="M5 10v10h14V10M9 20v-6h6v6" /></>,
  minus: <path d="M5 12h14" />,
  plus: <><path d="M12 5v14M5 12h14" /></>,
  search: <><circle cx="10.5" cy="10.5" r="6.5" /><path d="m16 16 5 5" /></>,
  user: <><circle cx="12" cy="8" r="4" /><path d="M4 21a8 8 0 0 1 16 0" /></>,
  warning: <><path d="M12 3 2.5 20h19Z" /><path d="M12 9v5M12 17.5v.5" /></>,
}

function Icon({ name, size = 22 }: { name: IconName; size?: number }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" aria-hidden>{iconPaths[name]}</svg>
}

const money = (value: number) => 'S/ ' + new Intl.NumberFormat('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value)

const roleLabel: Record<Role, string> = { mozo: 'Mozo', cocinero: 'Cocinero', almacenero: 'Almacenero' }
const wasteReasons = ['Quemado','Derramado','Vencido','Mal manejo','Sobrante','Devolución','Cortesía','Plato anulado']

function useTablet() {
  const [tablet, setTablet] = useState(() => window.matchMedia('(min-width: 768px)').matches)
  useEffect(() => {
    const media = window.matchMedia('(min-width: 768px)')
    const update = () => setTablet(media.matches)
    media.addEventListener('change', update)
    return () => media.removeEventListener('change', update)
  }, [])
  return tablet
}

const dishes = [
  { id: 1, name: 'Lomo saltado', desc: 'Lomo fino, papas y arroz', price: 32, tag: 'Más pedido', category: 'Fondos' },
  { id: 2, name: 'Ají de gallina', desc: 'Con arroz y huevo', price: 26, category: 'Fondos' },
  { id: 3, name: 'Ceviche clásico', desc: 'Pesca del día y camote', price: 34, tag: 'Fresco', category: 'Entradas' },
  { id: 4, name: 'Tequeños', desc: '6 unidades, salsa de palta', price: 18, category: 'Entradas' },
  { id: 5, name: 'Chicha morada', desc: 'Jarra de 1 litro', price: 14, category: 'Bebidas' },
  { id: 6, name: 'Limonada hierbabuena', desc: 'Jarra de 1 litro', price: 16, category: 'Bebidas' },
]

/* ───────────── Datos y estado compartido (un solo estado para los tres roles) ───────────── */
type LineState = 'pendiente' | 'preparando' | 'listo' | 'entregado' | 'anulado'
type Line = { id: number; dish: string; qty: number; price: number; note?: string; state: LineState; reason?: string }
type Orders = Record<number, Line[]>
type Waste = { id: number; dish: string; qty: number; table: number; reason: string; merma: boolean }
type Employee = { name: string; initials: string; role: Role; email: string }
type Insumo = { name: string; unit: string; stock: number; min: number; cost: number; last: string }
type Store = {
  orders: Orders
  times: Record<number, string>
  waste: Waste[]
  archive: { table: number; line: Line }[]
  inv: Insumo[]
  kardex: Record<string, Move[]>
  send: (table: number, lines: Omit<Line, 'id' | 'state'>[]) => void
  setLineState: (table: number, id: number, state: LineState) => void
  annul: (table: number, id: number, reason: string) => void
  deliver: (table: number) => void
  close: (table: number) => void
  ingreso: (name: string, qty: number, cost: number, supplier: string) => void
  conteo: (name: string, counted: number, obs: string) => void
  mermaIns: (name: string, qty: number, reason: string, detail: string) => void
  reset: () => void
}

const employees: Employee[] = [
  { name: 'José Cárdenas', initials: 'JC', role: 'mozo', email: 'jose@gastromind.pe' },
  { name: 'Camila Rojas', initials: 'CR', role: 'cocinero', email: 'camila@gastromind.pe' },
  { name: 'Mateo Quispe', initials: 'MQ', role: 'almacenero', email: 'mateo@gastromind.pe' },
]
const EmpCtx = createContext<Employee>(employees[0])
const StoreCtx = createContext<Store | null>(null)

const stateLabel: Record<LineState, string> = { pendiente: 'Recibido', preparando: 'En preparación', listo: 'Listo', entregado: 'Entregado', anulado: 'Anulado' }
const tableLabel = { libre: 'Libre', cocina: 'En cocina', lista: 'Por cobrar' } as const

const seedOrders: Orders = {
  2: [
    { id: 1, dish: 'Lomo saltado', qty: 2, price: 32, note: '1 sin cebolla', state: 'entregado' },
    { id: 2, dish: 'Limonada hierbabuena', qty: 1, price: 16, state: 'entregado' },
  ],
  3: [
    { id: 3, dish: 'Ceviche clásico', qty: 1, price: 34, note: 'Sin picante', state: 'preparando' },
    { id: 4, dish: 'Chicha morada', qty: 1, price: 14, state: 'listo' },
  ],
  4: [
    { id: 5, dish: 'Lomo saltado', qty: 2, price: 32, state: 'listo' },
    { id: 6, dish: 'Ají de gallina', qty: 1, price: 26, state: 'listo' },
    { id: 7, dish: 'Chicha morada', qty: 1, price: 14, state: 'listo' },
  ],
  6: [{ id: 8, dish: 'Ají de gallina', qty: 2, price: 26, state: 'pendiente' }],
  8: [
    { id: 9, dish: 'Lomo saltado', qty: 2, price: 32, note: '1 sin cebolla', state: 'preparando' },
    { id: 10, dish: 'Tequeños', qty: 1, price: 18, state: 'pendiente' },
  ],
}
const seedTimes: Record<number, string> = { 2: '32 min', 3: '18 min', 4: '41 min', 6: '09 min', 8: '24 min' }
const tables = [
  { n: 1, seats: 2 }, { n: 2, seats: 4 }, { n: 3, seats: 4 }, { n: 4, seats: 6 },
  { n: 5, seats: 2 }, { n: 6, seats: 4 }, { n: 7, seats: 8 }, { n: 8, seats: 4 },
]

const active = (ls: Line[] = []) => ls.filter(l => l.state !== 'anulado')
const billTotal = (ls: Line[] = []) => active(ls).reduce((s, l) => s + l.price * l.qty, 0)
function tableStatus(ls?: Line[]): 'libre' | 'cocina' | 'lista' {
  const a = active(ls)
  if (!a.length) return 'libre'
  return a.every(l => l.state === 'listo' || l.state === 'entregado') ? 'lista' : 'cocina'
}
const cuentaPlatos = (ls: Line[]) => ls.reduce((s, l) => s + l.qty, 0)

const STORE_KEY = 'gastromind-demo-v3'
const nowLabel = () => new Date().toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit', hour12: false })
const fmt = (n: number) => n.toFixed(2).replace('.', ',')
const readSaved = () => { try { return JSON.parse(localStorage.getItem(STORE_KEY) || 'null') } catch { return null } }

function useStore(): Store {
  const [init] = useState(readSaved)
  const [orders, setOrders] = useState<Orders>(init?.orders ?? seedOrders)
  const [times, setTimes] = useState<Record<number, string>>(init?.times ?? seedTimes)
  const [waste, setWaste] = useState<Waste[]>(init?.waste ?? [])
  const [archive, setArchive] = useState<{ table: number; line: Line }[]>(init?.archive ?? [])
  const [inv, setInv] = useState<Insumo[]>(init?.inv ?? seedInv)
  const [kardex, setKardex] = useState<Record<string, Move[]>>(init?.kardex ?? kardexData)
  useEffect(() => { try { localStorage.setItem(STORE_KEY, JSON.stringify({ orders, times, waste, archive, inv, kardex })) } catch { /* sin almacenamiento */ } }, [orders, times, waste, archive, inv, kardex])
  const move = (name: string, m: Move) => setKardex(k => ({ ...k, [name]: [m, ...(k[name] || [])] }))
  const patchInv = (name: string, f: (i: Insumo) => Insumo) => setInv(list => list.map(i => i.name === name ? f(i) : i))
  return {
    orders, times, waste, archive, inv, kardex,
    send: (table, lines) => {
      const base = Date.now()
      setOrders(o => ({ ...o, [table]: [...(o[table] || []), ...lines.map((l, i) => ({ ...l, id: base + i, state: 'pendiente' as LineState }))] }))
      setTimes(t => ({ ...t, [table]: t[table] || '01 min' }))
    },
    setLineState: (table, id, state) => setOrders(o => ({ ...o, [table]: (o[table] || []).map(l => l.id === id ? { ...l, state } : l) })),
    annul: (table, id, reason) => {
      const line = (orders[table] || []).find(l => l.id === id)
      if (!line) return
      setWaste(w => [...w, { id: Date.now(), dish: line.dish, qty: line.qty, table, reason, merma: line.state !== 'pendiente' }])
      setOrders(o => ({ ...o, [table]: o[table].map(l => l.id === id ? { ...l, state: 'anulado' as LineState, reason } : l) }))
    },
    deliver: table => setOrders(o => ({ ...o, [table]: (o[table] || []).map(l => l.state === 'listo' ? { ...l, state: 'entregado' as LineState } : l) })),
    close: table => {
      setArchive(a => [...a, ...(orders[table] || []).map(line => ({ table, line }))])
      setOrders(o => { const n = { ...o }; delete n[table]; return n })
      setTimes(t => { const n = { ...t }; delete n[table]; return n })
    },
    ingreso: (name, qty, cost, supplier) => {
      const it = inv.find(i => i.name === name); if (!it) return
      const stock = it.stock + qty
      patchInv(name, i => ({ ...i, stock, cost, last: `Hoy, ${nowLabel()}` }))
      move(name, { type: 'Compra', date: `Hoy, ${nowLabel()}`, days: 0, detail: `Proveedor ${supplier}`, qty: `+${fmt(qty)} ${it.unit}`, balance: `${fmt(stock)} ${it.unit}`, value: qty * cost, in: true })
    },
    conteo: (name, counted, obs) => {
      const it = inv.find(i => i.name === name); if (!it) return
      const diff = counted - it.stock
      patchInv(name, i => ({ ...i, stock: counted }))
      move(name, { type: 'Conteo', date: `Hoy, ${nowLabel()}`, days: 0, detail: obs || 'Ajuste por conteo físico', qty: `${diff >= 0 ? '+' : '−'}${fmt(Math.abs(diff))} ${it.unit}`, balance: `${fmt(counted)} ${it.unit}`, value: Math.abs(diff) * it.cost, in: diff >= 0 })
    },
    mermaIns: (name, qty, reason, detail) => {
      const it = inv.find(i => i.name === name); if (!it) return
      const stock = Math.max(0, it.stock - qty)
      patchInv(name, i => ({ ...i, stock }))
      move(name, { type: 'Merma', date: `Hoy, ${nowLabel()}`, days: 0, detail: detail ? `${reason} · ${detail}` : reason, qty: `−${fmt(qty)} ${it.unit}`, balance: `${fmt(stock)} ${it.unit}`, value: qty * it.cost })
    },
    reset: () => {
      setOrders(seedOrders); setTimes(seedTimes); setWaste([]); setArchive([]); setInv(seedInv); setKardex(kardexData)
    },
  }
}

/* ───────────── Piezas comunes ───────────── */
function Brand({ compact = false }: { compact?: boolean }) {
  return <div className="brand"><div className="brand-mark"><img src="/logo.svg" alt="GastroMind" /></div>{!compact && <div><b>GastroMind</b><small>OPERACIONES</small></div>}</div>
}

function Login({ sessionEnded, onLogin }: { sessionEnded: boolean; onLogin: (e: Employee) => void }) {
  const [method, setMethod] = useState<'pin' | 'correo'>('pin')
  const [pin, setPin] = useState('')
  const [idx, setIdx] = useState(0)
  const emp = employees[idx]
  const submit = () => onLogin(emp)
  return <main className="login-page">
    <section className="login-brand-panel">
      <Brand />
      <div className="login-message">
        <span className="eyebrow light">OPERACIÓN DEL RESTAURANTE</span>
        <h1>Tu servicio,<br />bajo control.</h1>
        <p>Costos, inventario y pedidos conectados en un solo lugar.</p>
      </div>
      <p className="login-foot">Sede Central · Lima</p>
    </section>
    <section className="login-form-panel">
      <div className="mobile-brand"><Brand /></div>
      <div className="login-card">
        {sessionEnded && <div className="session-note"><Icon name="check" size={18} /> Sesión cerrada correctamente</div>}
        <span className="eyebrow">BIENVENIDO</span>
        <h2>Ingresa a tu turno</h2>
        <p className="muted">Usa tu acceso rápido o correo corporativo.</p>
        <div className="segment">
          <button className={method === 'pin' ? 'active' : ''} onClick={() => setMethod('pin')}>PIN de empleado</button>
          <button className={method === 'correo' ? 'active' : ''} onClick={() => setMethod('correo')}>Correo</button>
        </div>
        {method === 'pin' ? <>
          <div className="employee-row">
            <div className="avatar">{emp.initials}</div><div><b>{emp.name}</b><span>{roleLabel[emp.role]} · Sede Central</span></div>
            <button className="text-button" onClick={() => setIdx((idx + 1) % employees.length)}>Cambiar</button>
          </div>
          <label>PIN de 4 dígitos</label>
          <div className="pin-display">{[0, 1, 2, 3].map(i => <span key={i} className={pin.length > i ? 'filled' : ''} />)}</div>
          <div className="pin-pad">
            {[1,2,3,4,5,6,7,8,9].map(n => <button key={n} onClick={() => setPin((pin + n).slice(0,4))}>{n}</button>)}
            <i /><button onClick={() => setPin((pin + '0').slice(0,4))}>0</button><button aria-label="Borrar" onClick={() => setPin(pin.slice(0,-1))}>⌫</button>
          </div>
          <button className="primary full" disabled={pin.length < 4} onClick={submit}>Ingresar al turno <Icon name="arrow" /></button>
        </> : <>
          <label>Correo corporativo</label><input className="input" key={emp.email} defaultValue={emp.email} type="email" />
          <label>Contraseña</label><input className="input" defaultValue="Demo2026!" type="password" />
          <button className="primary full" onClick={submit}>Ingresar <Icon name="arrow" /></button>
        </>}
      </div>
    </section>
  </main>
}

type Notice = { id: string; title: string; detail: string }
function useNotices(role: Role): Notice[] {
  const store = useContext(StoreCtx)
  if (!store) return []
  const out: Notice[] = []
  if (role === 'mozo') {
    Object.entries(store.orders).forEach(([t, ls]) => {
      const listos = ls.filter(l => l.state === 'listo')
      if (listos.length) out.push({ id: `l${t}`, title: `Mesa ${t}: listo para llevar`, detail: `${cuentaPlatos(listos)} platillo${cuentaPlatos(listos) > 1 ? 's' : ''} esperando en cocina` })
      else if (tableStatus(ls) === 'lista' && active(ls).every(l => l.state === 'entregado')) out.push({ id: `c${t}`, title: `Mesa ${t}: lista para cobrar`, detail: `Cuenta de ${money(billTotal(ls))}` })
    })
  } else if (role === 'cocinero') {
    Object.entries(store.orders).forEach(([t, ls]) => {
      const pend = ls.filter(l => l.state === 'pendiente')
      const demorada = parseInt(store.times[+t] || '0') >= 20 && ls.some(l => l.state === 'pendiente' || l.state === 'preparando')
      if (demorada) out.push({ id: `d${t}`, title: `Mesa ${t}: comanda demorada`, detail: `Lleva ${store.times[+t]} en cocina` })
      else if (pend.length) out.push({ id: `p${t}`, title: `Mesa ${t}: nueva comanda`, detail: `${cuentaPlatos(pend)} platillo${cuentaPlatos(pend) > 1 ? 's' : ''} por empezar` })
    })
  } else {
    store.inv.filter(i => i.stock < i.min).forEach(i => out.push({ id: `s${i.name}`, title: `${i.name}: bajo el mínimo`, detail: `Quedan ${fmt(i.stock)} ${i.unit} de ${fmt(i.min)} ${i.unit}` }))
    store.waste.filter(w => w.merma).forEach(w => out.push({ id: `w${w.id}`, title: `Merma por anulación: ${w.qty}× ${w.dish}`, detail: `Mesa ${w.table} · ${w.reason}` }))
  }
  return out
}

function AppHeader({ role, title, onProfile }: { role: Role; title: string; onProfile: () => void }) {
  const emp = useContext(EmpCtx)
  const notices = useNotices(role)
  const [open, setOpen] = useState(false)
  return <header className="app-header">
    <div><span className="location">GASTROMIND · SEDE CENTRAL</span><h1>{title}</h1></div>
    <div className="header-actions"><button className="icon-button notification" aria-label="Notificaciones" onClick={() => setOpen(true)}><Icon name="bell" />{notices.length > 0 && <i />}</button><button className="profile" onClick={onProfile}><span>{emp.initials}</span><div><b>{emp.name.split(' ')[0]}</b><small>{roleLabel[role]}</small></div><Icon name="chevron" size={16} /></button></div>
    {open && <div className="modal-backdrop align-right" onClick={() => setOpen(false)}><aside className="profile-panel notices" onClick={e => e.stopPropagation()}>
      <div className="profile-panel-head"><h3>Notificaciones</h3><button className="icon-button" onClick={() => setOpen(false)}><Icon name="close" /></button></div>
      {notices.length === 0 ? <div className="mini-empty"><Icon name="check" /><b>Todo al día</b><span>No tienes avisos pendientes.</span></div> : <div className="notice-list">{notices.map(n => <div className="notice" key={n.id}><span className="notice-dot" /><div><b>{n.title}</b><small>{n.detail}</small></div></div>)}</div>}
    </aside></div>}
  </header>
}

function BottomNav({ items, active, onChange }: { items: { id: string; label: string; icon: IconName }[]; active: string; onChange: (id: string) => void }) {
  const buttons = items.map(item => <button key={item.id} className={active === item.id ? 'active' : ''} onClick={() => onChange(item.id)}><Icon name={item.icon} /><span>{item.label}</span></button>)
  return <><nav className="bottom-nav">{buttons}</nav><nav className="nav-rail"><Brand compact />{buttons}</nav></>
}

function StateView({ state, onRetry }: { state: Exclude<DemoState, 'contenido'>; onRetry: () => void }) {
  if (state === 'cargando') return <div className="state-view"><div className="loader" /><h3>Actualizando información</h3><p>Un momento, estamos sincronizando con el restaurante.</p></div>
  if (state === 'vacio') return <div className="state-view"><div className="state-icon"><Icon name="check" size={30} /></div><h3>Todo está al día</h3><p>No hay registros para mostrar en este momento.</p></div>
  return <div className="state-view"><div className="state-icon error"><Icon name="warning" size={30} /></div><h3>No pudimos cargar la información</h3><p>Revisa tu conexión e inténtalo nuevamente.</p><button className="secondary" onClick={onRetry}>Reintentar</button></div>
}

function AnnulModal({ dish, state, table, onCancel, onConfirm }: { dish: string; state: LineState; table: number; onCancel: () => void; onConfirm: (reason: string) => void }) {
  const [reason, setReason] = useState('')
  const merma = state !== 'pendiente'
  return <div className="modal-backdrop"><div className="annul-modal">
    <div className="modal-heading"><div className="modal-icon danger"><Icon name="warning" /></div><button className="icon-button" onClick={onCancel}><Icon name="close" /></button></div>
    <span className="eyebrow">{merma ? 'REGISTRO DE MERMA' : 'ANULAR DEL PEDIDO'}</span><h3>Anular {dish}</h3>
    <p>{merma ? `Mesa ${table}: el platillo ya está ${state === 'preparando' ? 'en preparación' : 'preparado'}. Sus insumos quedarán registrados como merma.` : `Mesa ${table}: aún no empieza a prepararse, solo se retira de la comanda (sin merma).`}</p>
    <label>Motivo de anulación</label><div className="reason-grid">{wasteReasons.map(r => <button className={reason === r ? 'active' : ''} onClick={() => setReason(r)} key={r}>{r}</button>)}</div>
    <div className="modal-actions"><button className="ghost" onClick={onCancel}>Volver</button><button className="danger-button" disabled={!reason} onClick={() => onConfirm(reason)}>Anular platillo</button></div>
  </div></div>
}

/* ───────────── Mozo ───────────── */
function AccountDetail({ n, store, onAdd, onClose }: { n: number; store: Store; onAdd: () => void; onClose: () => void }) {
  const lines = store.orders[n] || []
  const act = active(lines)
  const subtotal = billTotal(lines)
  const [mode, setMode] = useState<'%' | 'S/'>('%')
  const [val, setVal] = useState('')
  const [why, setWhy] = useState('')
  const [receipt, setReceipt] = useState<null | { subtotal: number; disc: number; total: number; why: string }>(null)
  const num = parseFloat(val.replace(',', '.')) || 0
  const disc = Math.min(subtotal, mode === '%' ? subtotal * Math.min(num, 100) / 100 : num)
  const total = subtotal - disc
  const missing = act.filter(l => l.state !== 'entregado').reduce((s, l) => s + l.qty, 0)
  if (receipt) return <div className="account-success"><div className="state-icon"><Icon name="check" /></div><h3>Mesa {n} cerrada</h3>
    <div className="bill-lines receipt"><div><span>Subtotal</span><b className="money">{money(receipt.subtotal)}</b></div>{receipt.disc > 0 && <div><span>Descuento{receipt.why ? ` · ${receipt.why}` : ''}</span><b className="money">−{money(receipt.disc)}</b></div>}<div><span>Total cobrado</span><b className="money">{money(receipt.total)}</b></div></div>
    <button className="primary full" onClick={() => { store.close(n); onClose() }}>Volver al salón</button></div>
  return <div className="account-detail">
    <div className="account-heading"><div><span className="eyebrow">CUENTA ABIERTA</span><h2>Mesa {n}</h2></div><span className="status-label">{tableLabel[tableStatus(lines)]} · {store.times[n]}</span></div>
    <div className="guest-line"><span>{cuentaPlatos(act)} platillos</span><span>Pedido #{1040 + n}</span></div>
    <div className="bill-lines">{lines.map(l => <div key={l.id} className={l.state === 'anulado' ? 'line-struck' : ''}><span>{l.qty}× {l.dish} <small>{l.state === 'anulado' ? `Anulado · ${l.reason}` : [l.note, stateLabel[l.state]].filter(Boolean).join(' · ')}</small></span><b className="money">{money(l.price * l.qty)}</b></div>)}</div>
    <button className="secondary full" onClick={onAdd}><Icon name="plus" size={18} /> Agregar platillos</button>
    <div className="discount-box">
      <label>Descuento (opcional)</label>
      <div className="discount-row">
        <div className="mode-switch">{(['%', 'S/'] as const).map(m => <button key={m} className={mode === m ? 'active' : ''} onClick={() => setMode(m)}>{m}</button>)}</div>
        <input className="input" inputMode="decimal" placeholder={mode === '%' ? 'Ej. 10' : 'Ej. 5,00'} value={val} onChange={e => setVal(e.target.value)} />
      </div>
      {disc > 0 && <input className="input" placeholder="Motivo (cortesía, error de cocina…)" value={why} onChange={e => setWhy(e.target.value)} />}
    </div>
    <div className="bill-sum"><div><span>Subtotal</span><b className="money">{money(subtotal)}</b></div>{disc > 0 && <div><span>Descuento</span><b className="money">−{money(disc)}</b></div>}</div>
    <div className="bill-total"><span>Total de la cuenta</span><strong className="money">{money(total)}</strong></div>
    {missing > 0 && <p className="hint">Faltan {missing} platillo{missing > 1 ? 's' : ''} por entregar antes de cobrar.</p>}
    <button className="primary full" disabled={missing > 0 || !act.length} onClick={() => setReceipt({ subtotal, disc, total, why })}>Cerrar mesa y cobrar <Icon name="arrow" /></button>
  </div>
}

function Mozo({ demoState, onProfile, store }: { demoState: DemoState; onProfile: () => void; store: Store }) {
  const isTablet = useTablet()
  const [view, setView] = useState('salon')
  const [selectedTable, setSelectedTable] = useState<number | null>(null)
  const [focusedTable, setFocusedTable] = useState(2)
  const [accountTable, setAccountTable] = useState<number | null>(null)
  const [category, setCategory] = useState('Todos')
  const [query, setQuery] = useState('')
  const [cart, setCart] = useState<Record<number, number>>({})
  const [notes, setNotes] = useState<Record<number, string>>({})
  const [noteFor, setNoteFor] = useState<number | null>(null)
  const [noteText, setNoteText] = useState('')
  const [annulCart, setAnnulCart] = useState<number | null>(null)
  const [annulLine, setAnnulLine] = useState<{ table: number; line: Line } | null>(null)
  const [reason, setReason] = useState('')
  const [orderOpen, setOrderOpen] = useState(false)
  const cartCount = Object.values(cart).reduce((a, b) => a + b, 0)
  const total = dishes.reduce((sum, d) => sum + d.price * (cart[d.id] || 0), 0)
  const add = (id: number) => setCart(c => ({ ...c, [id]: (c[id] || 0) + 1 }))
  const remove = (id: number) => setCart(c => ({ ...c, [id]: Math.max(0, (c[id] || 0) - 1) }))
  const statusOf = (n: number) => tableStatus(store.orders[n])
  const counts = { libres: tables.filter(t => statusOf(t.n) === 'libre').length, cocina: tables.filter(t => statusOf(t.n) === 'cocina').length, cobrar: tables.filter(t => statusOf(t.n) === 'lista').length }
  const send = () => {
    if (selectedTable === null) return
    store.send(selectedTable, dishes.filter(d => cart[d.id]).map(d => ({ dish: d.name, qty: cart[d.id], price: d.price, note: notes[d.id] || undefined })))
    setCart({}); setNotes({}); setQuery(''); setOrderOpen(false); setSelectedTable(null); setView('cocina')
  }

  if (selectedTable !== null) return <div className="app-shell">
    <header className="order-header"><button className="icon-button" onClick={() => setSelectedTable(null)}>←</button><div><span className="location">{(store.orders[selectedTable] || []).length ? 'AGREGAR PLATILLOS' : 'NUEVO PEDIDO'}</span><h1>Mesa {selectedTable}</h1></div><button className="text-button" onClick={() => setOrderOpen(true)}>Ver pedido · {cartCount}</button></header>
    <main className="order-layout">
      <section className="menu-area">
        <div className="search"><Icon name="search" size={19} /><input placeholder="Buscar en la carta" value={query} onChange={e => setQuery(e.target.value)} /></div>
        <div className="category-scroll">{['Todos','Entradas','Fondos','Bebidas'].map(c => <button className={category === c ? 'active' : ''} onClick={() => setCategory(c)} key={c}>{c}</button>)}</div>
        <div className="dish-grid">{dishes.filter(d => (category === 'Todos' || d.category === category) && d.name.toLowerCase().includes(query.toLowerCase())).map(d => <article className="dish-card" key={d.id}>
          <div>{d.tag && <span className="dish-tag">{d.tag}</span>}<h3>{d.name}</h3><p>{d.desc}</p></div>
          <footer><b className="money">{money(d.price)}</b>{cart[d.id] ? <div className="stepper"><button onClick={() => remove(d.id)}><Icon name="minus" size={17} /></button><b>{cart[d.id]}</b><button onClick={() => add(d.id)}><Icon name="plus" size={17} /></button></div> : <button className="add-button" onClick={() => add(d.id)}><Icon name="plus" size={18} /></button>}</footer>
        </article>)}</div>
      </section>
      <aside className={`order-panel ${orderOpen ? 'open' : ''}`}>
        <div className="panel-title"><div><span className="location">COMANDA ACTUAL</span><h2>Mesa {selectedTable}</h2></div><button className="icon-button mobile-only" onClick={() => setOrderOpen(false)}><Icon name="close" /></button></div>
        {cartCount === 0 ? <div className="mini-empty"><Icon name="cutlery" /><b>Pedido vacío</b><span>Agrega platillos de la carta.</span></div> : <div className="cart-lines">{dishes.filter(d => cart[d.id]).map(d => <div className="cart-line" key={d.id}><span className="qty">{cart[d.id]}</span><div><b>{d.name}</b>{notes[d.id] && <small className="item-note">{notes[d.id]}</small>}<button onClick={() => { setNoteFor(d.id); setNoteText(notes[d.id] || '') }}>{notes[d.id] ? 'Editar nota' : '+ Agregar nota'}</button></div><strong className="money">{money(d.price * cart[d.id])}</strong><button className="cart-annul" aria-label={`Quitar ${d.name}`} onClick={() => setAnnulCart(d.id)}><Icon name="close" size={16}/></button></div>)}</div>}
        <div className="order-total"><span>Total estimado</span><strong className="money">{money(total)}</strong></div>
        <button className="primary full" disabled={!cartCount} onClick={send}>Enviar a cocina <Icon name="arrow" /></button>
      </aside>
      {orderOpen && <div className="drawer-scrim" onClick={() => setOrderOpen(false)} />}
    </main>
    {cartCount > 0 && <button className="floating-order" onClick={() => setOrderOpen(true)}><span>{cartCount} productos</span><b>{money(total)}</b></button>}
    {noteFor !== null && <div className="modal-backdrop"><div className="note-modal"><span className="eyebrow">INDICACIÓN PARA COCINA</span><h3>Nota para {dishes.find(d=>d.id===noteFor)?.name}</h3><label>Detalle del platillo</label><input autoFocus value={noteText} onChange={e=>setNoteText(e.target.value)} placeholder="Ej. sin cebolla" /><div className="quick-notes">{['Sin cebolla','Sin picante','Bien cocido'].map(n=><button onClick={()=>setNoteText(n)} key={n}>{n}</button>)}</div><div className="modal-actions"><button className="ghost" onClick={()=>setNoteFor(null)}>Cancelar</button><button className="primary" onClick={()=>{setNotes(n=>({...n,[noteFor]:noteText}));setNoteFor(null)}}>Guardar nota</button></div></div></div>}
    {annulCart !== null && <div className="modal-backdrop"><div className="annul-modal"><div className="modal-heading"><div className="modal-icon danger"><Icon name="warning"/></div><button className="icon-button" onClick={()=>setAnnulCart(null)}><Icon name="close"/></button></div><span className="eyebrow">QUITAR DEL PEDIDO</span><h3>{dishes.find(d=>d.id===annulCart)?.name}</h3><p>Aún no se envía a cocina, solo se quita del pedido (sin merma).</p><div className="modal-actions"><button className="ghost" onClick={()=>setAnnulCart(null)}>Volver</button><button className="danger-button" onClick={()=>{setCart(c=>({...c,[annulCart]:0}));setAnnulCart(null)}}>Quitar platillo</button></div></div></div>}
  </div>

  const ready = Object.entries(store.orders).find(([, ls]) => ls.some(l => l.state === 'listo'))
  const orderEntries = Object.entries(store.orders).filter(([, ls]) => tableStatus(ls) !== 'libre')
  return <div className="app-shell">
    <AppHeader role="mozo" title={view === 'salon' ? 'Salón' : view === 'cocina' ? 'En cocina' : 'Mi turno'} onProfile={onProfile} />
    <main className="content">
      {demoState !== 'contenido' ? <StateView state={demoState} onRetry={() => {}} /> : view === 'salon' ? <>
        <div className="status-summary"><div><b>{tables.length}</b><span>mesas</span></div><div><b className="green">{counts.libres}</b><span>libres</span></div><div><b className="amber">{counts.cocina}</b><span>en cocina</span></div><div><b>{counts.cobrar}</b><span>por cobrar</span></div></div>
        <div className="section-title"><div><span className="eyebrow">MAPA DEL SALÓN</span><h2>Mesas</h2></div><div className="legend"><span><i className="dot free" />Libre</span><span><i className="dot busy" />En cocina</span><span><i className="dot ready" />Por cobrar</span></div></div>
        <div className="salon-master"><div className="tables-grid">{tables.map(t => { const st = statusOf(t.n); return <button key={t.n} className={`table-card ${st === 'cocina' ? 'ocupada' : st} ${focusedTable === t.n ? 'selected' : ''}`} onClick={() => { if (isTablet) setFocusedTable(t.n); else if (st === 'libre') setSelectedTable(t.n); else setAccountTable(t.n) }}>
          <div className="table-top"><span className="table-number">{t.n}</span><span className="status-label">{tableLabel[st]}</span></div>
          <div className="chairs">{Array.from({ length: Math.min(t.seats, 6) }).map((_,i) => <i key={i} />)}</div>
          <footer>{st !== 'libre' ? <><span><Icon name="clock" size={15} />{store.times[t.n]}</span><b className="money">{money(billTotal(store.orders[t.n]))}</b></> : <><span>{t.seats} personas</span><b>Abrir</b></>}</footer>
        </button> })}</div><aside className="table-detail">{statusOf(focusedTable) === 'libre' ? <div className="free-detail"><div className="state-icon"><Icon name="grid"/></div><span className="eyebrow">MESA DISPONIBLE</span><h2>Mesa {focusedTable}</h2><p>Lista para recibir un nuevo pedido.</p><button className="primary full" onClick={()=>setSelectedTable(focusedTable)}>Abrir mesa</button></div> : <AccountDetail key={focusedTable} n={focusedTable} store={store} onAdd={()=>setSelectedTable(focusedTable)} onClose={()=>{}} />}</aside></div>
      </> : view === 'cocina' ? <>
        {ready && <div className="ready-banner"><div className="ready-icon"><Icon name="bell" /></div><div><span>LISTO PARA LLEVAR</span><b>Mesa {ready[0]} · {cuentaPlatos(ready[1].filter(l => l.state === 'listo'))} platillos</b></div><button onClick={() => store.deliver(+ready[0])}>Confirmar entrega</button></div>}
        <div className="section-title"><div><span className="eyebrow">SEGUIMIENTO EN VIVO</span><h2>Pedidos del salón</h2></div><span className="live"><i /> En vivo</span></div>
        {orderEntries.length === 0 ? <div className="mini-empty"><Icon name="cutlery" /><b>Sin pedidos abiertos</b><span>Abre una mesa para tomar un pedido.</span></div> : <div className="tracking-list">
          {orderEntries.map(([t, ls]) => <article className="order-card" key={t}>
            <div className="order-card-head"><div className="table-badge">M{t}</div><div><b>Mesa {t}</b><small>{store.times[+t]}</small></div><span className="status-label">{tableLabel[tableStatus(ls)]}</span></div>
            {ls.map(l => <div className={`order-line ${l.state}`} key={l.id}>
              <div><b>{l.qty}× {l.dish}</b>{l.note && <small>{l.note}</small>}{l.state === 'anulado' && <small>Anulado · {l.reason}</small>}</div>
              <span className="line-state">{stateLabel[l.state]}</span>
              {(l.state === 'pendiente' || l.state === 'preparando' || l.state === 'listo') && <button className="annul-link" onClick={() => { setReason(''); setAnnulLine({ table: +t, line: l }) }}>Anular</button>}
            </div>)}
            <button className="text-button" onClick={() => setSelectedTable(+t)}>+ Agregar platillos</button>
          </article>)}
        </div>}
      </> : <TurnSummary store={store} />}
    </main>
    <BottomNav active={view} onChange={setView} items={[{id:'salon',label:'Salón',icon:'grid'},{id:'cocina',label:'En cocina',icon:'cutlery'},{id:'turno',label:'Mi turno',icon:'history'}]} />
    {accountTable !== null && <div className="modal-backdrop mobile-account"><div className="account-modal"><button className="account-close icon-button" onClick={()=>setAccountTable(null)}><Icon name="close"/></button><AccountDetail n={accountTable} store={store} onAdd={()=>{setSelectedTable(accountTable);setAccountTable(null)}} onClose={()=>setAccountTable(null)}/></div></div>}
    {annulLine && <AnnulModal key={annulLine.line.id} dish={annulLine.line.dish} state={annulLine.line.state} table={annulLine.table} onCancel={() => setAnnulLine(null)} onConfirm={r => { store.annul(annulLine.table, annulLine.line.id, r); setAnnulLine(null) }} />}
  </div>
}

function TurnSummary({ store }: { store: Store }) {
  const closed = store.archive.reduce((s, a) => s + (a.line.state === 'anulado' ? 0 : a.line.price * a.line.qty), 0)
  return <div className="turn-summary">
    <h2>Resumen de tu turno</h2><p className="muted">Tu actividad de hoy, actualizada al momento.</p>
    <div className="big-stat"><span>Venta atendida</span><strong className="money">{money(1284 + closed)}</strong><small>{12 + new Set(store.archive.map(a => a.table)).size} mesas · 36 comensales</small></div>
    <div className="summary-row"><div><span>Ticket promedio</span><b className="money">S/ 107,00</b></div><div><span>Tiempo promedio</span><b>38 min</b></div></div>
    <h3>Últimas mesas</h3><div className="simple-list"><div><span>M4</span><b>Mesa 4</b><strong className="money">S/ 142,00</strong></div><div><span>M2</span><b>Mesa 2</b><strong className="money">S/ 86,00</strong></div></div>
  </div>
}

/* ───────────── Cocina ───────────── */
function Cocina({ demoState, onProfile, store }: { demoState: DemoState; onProfile: () => void; store: Store }) {
  const [view, setView] = useState('comandas')
  const [filter, setFilter] = useState('Todas')
  const [annul, setAnnul] = useState<{ table: number; line: Line } | null>(null)
  const commands = Object.entries(store.orders).map(([t, ls]) => ({ table: +t, code: `#${1040 + +t}`, time: store.times[+t] || '01 min', lines: ls, urgent: parseInt(store.times[+t] || '0') >= 20 && ls.some(l => l.state === 'pendiente' || l.state === 'preparando') }))
    .filter(c => c.lines.some(l => l.state === 'pendiente' || l.state === 'preparando' || l.state === 'listo'))
  const isReady = (c: typeof commands[number]) => active(c.lines).length > 0 && active(c.lines).every(l => l.state === 'listo' || l.state === 'entregado')
  const counts = { Todas: commands.length, Pendientes: commands.filter(c => c.lines.some(l => l.state === 'pendiente')).length, Preparando: commands.filter(c => c.lines.some(l => l.state === 'preparando')).length, Listas: commands.filter(isReady).length }
  const shown = commands.filter(c => filter === 'Todas' || (filter === 'Pendientes' && c.lines.some(l => l.state === 'pendiente')) || (filter === 'Preparando' && c.lines.some(l => l.state === 'preparando')) || (filter === 'Listas' && isReady(c)))
  const doneNow = Object.values(store.orders).flat().filter(l => l.state === 'listo' || l.state === 'entregado').length + store.archive.filter(a => a.line.state !== 'anulado').length
  const history = [...Object.entries(store.orders).flatMap(([t, ls]) => ls.filter(l => l.state === 'entregado' || l.state === 'anulado').map(line => ({ table: +t, line }))), ...store.archive]
  const title = view === 'comandas' ? 'Cocina' : view === 'historial' ? 'Historial del turno' : 'Resumen del turno'
  return <div className="app-shell kitchen-shell">
    <AppHeader role="cocinero" title={title} onProfile={onProfile} />
    {view === 'comandas' && <div className="kitchen-toolbar"><div className="kpi"><b>{commands.length}</b><span>comandas</span></div><div className="kpi urgent"><b>{commands.filter(c => c.urgent).length}</b><span>demorada</span></div><div className="filter-tabs">{Object.entries(counts).map(([k,v]) => <button className={filter === k ? 'active' : ''} onClick={() => setFilter(k)} key={k}>{k}<span>{v}</span></button>)}</div><span className="live"><i /> En vivo</span></div>}
    <main className="kitchen-content">
      {demoState !== 'contenido' ? <StateView state={demoState} onRetry={() => {}} /> : view === 'resumen' ? <KitchenSummary done={44 + doneNow} /> : view === 'historial' ? <div className="turn-summary kitchen-summary"><h2>Terminados y anulados</h2><p className="muted">Lo que ya salió de cocina en este turno.</p>{history.length === 0 ? <div className="mini-empty"><Icon name="check" /><b>Aún no hay registros</b><span>Los platillos entregados o anulados aparecen aquí.</span></div> : <div className="simple-list">{history.map(({ table, line }) => <div key={`${table}-${line.id}`}><span>M{table}</span><b>{line.qty}× {line.dish}{line.state === 'anulado' && <small className="reason-small"> · {line.reason}</small>}</b><strong className={line.state === 'anulado' ? 'bad' : ''}>{line.state === 'anulado' ? 'Anulado' : 'Servido'}</strong></div>)}</div>}</div>
        : commands.length === 0 ? <div className="state-view"><div className="state-icon"><Icon name="check" size={30} /></div><h3>Sin comandas pendientes</h3><p>Cuando el mozo envíe un pedido aparecerá aquí.</p></div>
        : <div className="command-grid">{shown.map(c => <article className={`command ${c.urgent ? 'urgent' : ''}`} key={c.table}>
        <header><div><span className="command-table">M{c.table}</span><div><b>Mesa {c.table}</b><small>{c.code}</small></div></div><span className="command-time"><Icon name="clock" size={17} /> {c.time}</span></header>
        <div className="command-items">{c.lines.map(l => <div className={`command-item ${l.state === 'pendiente' ? 'pendiente' : l.state === 'preparando' ? 'preparando' : 'listo'} ${l.state === 'anulado' ? 'line-struck' : ''}`} key={l.id}>
          <div className="item-main"><span className="item-qty">{l.qty}×</span><div><b>{l.dish}</b>{l.note && <small>{l.note}</small>}{l.state === 'anulado' && <small>Anulado · {l.reason}</small>}</div></div>
          {l.state !== 'anulado' && <div className="item-actions">{(l.state === 'pendiente' || l.state === 'preparando' || l.state === 'listo') && <button className="annul-button" onClick={() => setAnnul({ table: c.table, line: l })} aria-label="Anular platillo"><Icon name="close" size={18} /></button>}
            {l.state === 'listo' || l.state === 'entregado' ? <span className="done"><Icon name="check" size={17} /> {l.state === 'entregado' ? 'Entregado' : 'Listo'}</span> : <button className={l.state === 'preparando' ? 'state-button preparing' : 'state-button'} onClick={() => store.setLineState(c.table, l.id, l.state === 'pendiente' ? 'preparando' : 'listo')}>{l.state === 'preparando' ? 'Marcar listo' : 'Empezar'}</button>}
          </div>}
        </div>)}</div>
        <footer><span>{active(c.lines).filter(l => l.state === 'listo' || l.state === 'entregado').length} de {active(c.lines).length} listos</span><div className="progress"><i style={{width:`${active(c.lines).length ? active(c.lines).filter(l => l.state === 'listo' || l.state === 'entregado').length / active(c.lines).length * 100 : 0}%`}} /></div></footer>
      </article>)}</div>}
    </main>
    {annul && <AnnulModal key={annul.line.id} dish={annul.line.dish} state={annul.line.state} table={annul.table} onCancel={() => setAnnul(null)} onConfirm={r => { store.annul(annul.table, annul.line.id, r); setAnnul(null) }} />}
    <BottomNav active={view} onChange={setView} items={[{id:'comandas',label:'Comandas',icon:'cutlery'},{id:'historial',label:'Historial',icon:'history'},{id:'resumen',label:'Mi turno',icon:'user'}]} />
  </div>
}

function KitchenSummary({ done }: { done: number }) {
  const inTime = Math.round(done * 0.92)
  return <div className="turn-summary kitchen-summary"><h2>Ritmo de cocina</h2><p className="muted">Indicadores operativos del turno actual.</p><div className="summary-row"><div><span>Platillos terminados</span><b className="number">{done}</b></div><div><span>Tiempo promedio</span><b className="number">14 min</b></div></div><div className="big-stat"><span>Objetivo de servicio</span><strong className="number">92%</strong><small>{inTime} de {done} platillos dentro de 18 minutos</small></div></div>
}

/* ───────────── Almacén ───────────── */
type Move = { type: 'Compra' | 'Consumo' | 'Merma' | 'Conteo'; date: string; days: number; detail: string; qty: string; balance: string; value: number; in?: boolean }
const seedInv: Insumo[] = [
  { name: 'Aceite vegetal', unit: 'L', stock: 4.5, min: 8, cost: 9, last: 'Hace 6 días' },
  { name: 'Lomo fino', unit: 'kg', stock: 6.2, min: 5, cost: 34, last: 'Hoy, 09:42' },
  { name: 'Papa amarilla', unit: 'kg', stock: 18, min: 12, cost: 3, last: 'Ayer, 10:15' },
  { name: 'Limón', unit: 'kg', stock: 3.8, min: 6, cost: 8, last: 'Hace 9 días' },
]
const numOf = (t: string) => parseFloat(t.replace(',', '.')) || 0
const kardexData: Record<string, Move[]> = {
  'Aceite vegetal': [
    { type: 'Consumo', date: 'Hoy, 08:30', days: 0, detail: 'Producción de cocina', qty: '−1,50 L', balance: '4,50 L', value: 13.5 },
    { type: 'Merma', date: 'Ayer, 20:10', days: 1, detail: 'Derramado · Ana Torres', qty: '−0,50 L', balance: '6,00 L', value: 4.5 },
    { type: 'Compra', date: 'Hace 6 días', days: 6, detail: 'Proveedor Alicorp', qty: '+10,00 L', balance: '6,50 L', value: 90, in: true },
    { type: 'Conteo', date: 'Hace 20 días', days: 20, detail: 'Ajuste por conteo físico', qty: '−0,20 L', balance: '−', value: 1.8 },
  ],
  'Lomo fino': [
    { type: 'Compra', date: 'Hoy, 09:42', days: 0, detail: 'Proveedor San Fernando', qty: '+8,00 kg', balance: '6,20 kg', value: 272, in: true },
    { type: 'Consumo', date: 'Hoy, 08:16', days: 0, detail: 'Producción de cocina', qty: '−2,40 kg', balance: '4,60 kg', value: 81.6 },
    { type: 'Merma', date: 'Ayer, 21:08', days: 1, detail: 'Mal manejo · Ana Torres', qty: '−0,50 kg', balance: '7,00 kg', value: 17 },
    { type: 'Conteo', date: 'Ayer, 16:30', days: 1, detail: 'Ajuste por conteo físico', qty: '+0,30 kg', balance: '7,50 kg', value: 10.2, in: true },
    { type: 'Compra', date: 'Hace 12 días', days: 12, detail: 'Proveedor San Fernando', qty: '+10,00 kg', balance: '7,20 kg', value: 340, in: true },
  ],
  'Papa amarilla': [
    { type: 'Consumo', date: 'Hoy, 08:20', days: 0, detail: 'Producción de cocina', qty: '−3,00 kg', balance: '18,00 kg', value: 9 },
    { type: 'Compra', date: 'Ayer, 10:15', days: 1, detail: 'Mercado Santa Anita', qty: '+12,00 kg', balance: '21,00 kg', value: 36, in: true },
    { type: 'Consumo', date: 'Hace 5 días', days: 5, detail: 'Producción de cocina', qty: '−4,00 kg', balance: '9,00 kg', value: 12 },
  ],
  'Limón': [
    { type: 'Consumo', date: 'Hoy, 08:16', days: 0, detail: 'Producción de cocina', qty: '−2,40 kg', balance: '3,80 kg', value: 19.2 },
    { type: 'Merma', date: 'Hace 3 días', days: 3, detail: 'Vencido · Ana Torres', qty: '−0,60 kg', balance: '6,20 kg', value: 4.8 },
    { type: 'Compra', date: 'Hace 9 días', days: 9, detail: 'Mercado Santa Anita', qty: '+8,00 kg', balance: '6,80 kg', value: 64, in: true },
  ],
}

function Field({ label, placeholder, value }: { label: string; placeholder?: string; value?: string }) {
  return <label className="field">{label}<div>{value || placeholder}<Icon name="chevron" size={16} /></div></label>
}

function InsumoSelect({ inv, value, onChange }: { inv: Insumo[]; value: string; onChange: (v: string) => void }) {
  return <label className="field">Insumo<select className="select" value={value} onChange={e => onChange(e.target.value)}>{inv.map(i => <option key={i.name} value={i.name}>{i.name}</option>)}</select></label>
}

function IngresoForm({ store, name, setName, notify }: { store: Store; name: string; setName: (n: string) => void; notify: (t: string) => void }) {
  const it = store.inv.find(i => i.name === name) || store.inv[0]
  const [supplier, setSupplier] = useState('Mercado Santa Anita')
  const [qty, setQty] = useState('')
  const [cost, setCost] = useState('')
  const [doc, setDoc] = useState('')
  const unitCost = cost ? numOf(cost) : it.cost
  const total = numOf(qty) * unitCost
  return <FormCard eyebrow="NUEVO MOVIMIENTO" title="Datos de la compra" action="Registrar ingreso" disabled={numOf(qty) <= 0} onSave={() => { store.ingreso(it.name, numOf(qty), unitCost, supplier || 'sin proveedor'); notify(`Compra registrada: +${fmt(numOf(qty))} ${it.unit} de ${it.name}`); setQty(''); setCost(''); setDoc('') }}>
    <label className="field">Proveedor<input value={supplier} onChange={e => setSupplier(e.target.value)} /></label>
    <InsumoSelect inv={store.inv} value={it.name} onChange={setName} />
    <div className="field-row"><label className="field">Cantidad ({it.unit})<input inputMode="decimal" placeholder="Ej. 8,00" value={qty} onChange={e => setQty(e.target.value)} /></label><label className="field">Costo unitario<input inputMode="decimal" placeholder={fmt(it.cost)} value={cost} onChange={e => setCost(e.target.value)} /></label></div>
    <label className="field">Costo total<div className="calculated money">{money(total)}</div></label>
    <label className="field">N.° de comprobante<input placeholder="Ej. F001-00238" value={doc} onChange={e => setDoc(e.target.value)} /></label>
  </FormCard>
}

function ConteoForm({ store, name, setName, notify }: { store: Store; name: string; setName: (n: string) => void; notify: (t: string) => void }) {
  const it = store.inv.find(i => i.name === name) || store.inv[0]
  const [counted, setCounted] = useState('')
  const [obs, setObs] = useState('')
  const has = counted.trim() !== ''
  const diff = numOf(counted) - it.stock
  return <FormCard eyebrow="CONTROL DE STOCK" title="Nuevo conteo físico" action="Guardar conteo" disabled={!has} onSave={() => { store.conteo(it.name, numOf(counted), obs); notify(`Conteo guardado: ${it.name} ahora ${fmt(numOf(counted))} ${it.unit}`); setCounted(''); setObs('') }}>
    <InsumoSelect inv={store.inv} value={it.name} onChange={setName} />
    <div className="stock-reference"><span>Stock en sistema</span><b className="number">{fmt(it.stock)} {it.unit}</b></div>
    <label className="field focus-field">Cantidad contada ({it.unit})<input inputMode="decimal" placeholder="Ej. 3,80" value={counted} onChange={e => setCounted(e.target.value)} /></label>
    {has && <div className={`difference ${diff < 0 ? 'negative' : diff > 0 ? 'positive-diff' : ''}`}><span>Diferencia detectada</span><b className="number">{diff >= 0 ? '+' : '−'}{fmt(Math.abs(diff))} {it.unit}</b><small>{diff < 0 ? 'Faltante respecto al sistema' : diff > 0 ? 'Sobrante respecto al sistema' : 'Coincide con el sistema'}</small></div>}
    <label className="field">Observación<input placeholder="Agrega un comentario opcional" value={obs} onChange={e => setObs(e.target.value)} /></label>
  </FormCard>
}

function MermaForm({ store, name, setName, notify }: { store: Store; name: string; setName: (n: string) => void; notify: (t: string) => void }) {
  const it = store.inv.find(i => i.name === name) || store.inv[0]
  const [qty, setQty] = useState('')
  const [reason, setReason] = useState('')
  const [detail, setDetail] = useState('')
  const emp = useContext(EmpCtx)
  return <FormCard eyebrow="SALIDA DE INVENTARIO" title="Datos de la merma" action="Registrar merma" danger disabled={numOf(qty) <= 0 || !reason} onSave={() => { store.mermaIns(it.name, numOf(qty), reason, detail || emp.name); notify(`Merma registrada: −${fmt(numOf(qty))} ${it.unit} de ${it.name}`); setQty(''); setReason(''); setDetail('') }}>
    <InsumoSelect inv={store.inv} value={it.name} onChange={setName} />
    <label className="field">Cantidad ({it.unit})<input inputMode="decimal" placeholder="Ej. 0,60" value={qty} onChange={e => setQty(e.target.value)} /></label>
    <label className="field">Motivo</label><div className="reason-grid light">{wasteReasons.map(r => <button className={reason === r ? 'active' : ''} onClick={() => setReason(r)} key={r}>{r}</button>)}</div>
    <label className="field">Detalle<input placeholder="Describe brevemente lo ocurrido" value={detail} onChange={e => setDetail(e.target.value)} /></label>
  </FormCard>
}

function Almacen({ demoState, onProfile, store }: { demoState: DemoState; onProfile: () => void; store: Store }) {
  const [view, setView] = useState('stock')
  const [search, setSearch] = useState('')
  const [selectedIngredient, setSelectedIngredient] = useState('Aceite vegetal')
  const [saved, setSaved] = useState('')
  const notify = (text: string) => { setSaved(text); setTimeout(() => setSaved(''), 2600) }
  const isLow = (i: Insumo) => i.stock < i.min
  const lastMoves = (store.kardex[selectedIngredient] || []).slice(0, 2)
  const unitOf = store.inv.find(i => i.name === selectedIngredient)?.unit
  const mermas = store.waste.filter(w => w.merma)
  const totalValue = 8090.8 + store.inv.reduce((s, i) => s + i.stock * i.cost, 0)
  const lowCount = store.inv.filter(isLow).length
  return <div className="app-shell">
    <AppHeader role="almacenero" title={view === 'stock' ? 'Inventario' : view === 'kardex' ? 'Kardex' : view === 'ingreso' ? 'Ingreso de compras' : view === 'conteo' ? 'Conteo físico' : 'Registrar merma'} onProfile={onProfile} />
    <main className="content warehouse-content">
      {demoState !== 'contenido' ? <StateView state={demoState} onRetry={() => {}} /> : view === 'stock' ? <>
        {lowCount > 0 ? <div className="alert-strip"><Icon name="warning" /><div><b>{lowCount} insumo{lowCount > 1 ? 's' : ''} bajo el mínimo</b><span>Repónlos pronto para evitar quiebres de stock.</span></div><button onClick={() => setSearch('bajo')}>Ver alertas</button></div> : <div className="alert-strip ok"><Icon name="check" /><div><b>Todo el stock sobre el mínimo</b><span>No hay insumos por reponer.</span></div></div>}
        <div className="section-title"><div><span className="eyebrow">VALOR DE INVENTARIO</span><h2 className="money">{money(totalValue)}</h2></div><button className="secondary" onClick={() => setView('ingreso')}><Icon name="plus" size={18} /> Movimiento</button></div>
        <div className="search"><Icon name="search" size={19} /><input value={search === 'bajo' ? '' : search} onChange={e=>setSearch(e.target.value)} placeholder={search === 'bajo' ? 'Mostrando stock bajo' : 'Buscar insumo'} />{search === 'bajo' && <button onClick={()=>setSearch('')}><Icon name="close" size={17}/></button>}</div>
        <div className="inventory-master"><div><div className="inventory-list"><div className="inventory-head"><span>Insumo</span><span>Stock actual</span><span>Valor</span><span /></div>
          {store.inv.filter(i => search === 'bajo' ? isLow(i) : i.name.toLowerCase().includes(search.toLowerCase())).map(i => <div className={`inventory-row ${isLow(i) ? 'low' : ''} ${selectedIngredient === i.name ? 'selected' : ''}`} onClick={()=>setSelectedIngredient(i.name)} key={i.name}><div><span className="ingredient-icon"><Icon name="box" size={19}/></span><div><b>{i.name}</b>{isLow(i) && <small>BAJO EL MÍNIMO · mín. {i.min} {i.unit}</small>}</div></div><strong className="number">{fmt(i.stock)} {i.unit}</strong><span className="money">{money(i.stock * i.cost)}</span><button onClick={e=>{e.stopPropagation();setSelectedIngredient(i.name);setView('kardex')}} aria-label={`Ver Kardex de ${i.name}`}><Icon name="chevron" size={17}/></button></div>)}
        </div>
        <div className="kardex-card"><div className="section-title compact"><div><span className="eyebrow">KARDEX · {selectedIngredient.toUpperCase()}</span><h3>Últimos movimientos</h3></div><button className="text-button" onClick={()=>setView('kardex')}>Ver todos</button></div>
          {lastMoves.map(m => <div className="movement" key={m.date + m.type + m.qty}><span className={`move-icon ${m.in ? 'in' : 'out'}`}>{m.in ? '+' : '−'}</span><div><b>{m.type} · {selectedIngredient}</b><small>{m.date} · {m.detail}</small></div><strong className={`number ${m.in ? 'positive' : ''}`}>{m.qty}</strong><span className="money">{money(m.value)}</span></div>)}
        </div></div><InventoryDetail item={store.inv.find(i => i.name === selectedIngredient) || store.inv[0]} onKardex={()=>setView('kardex')} onMove={()=>setView('ingreso')} /></div>
      </> : view === 'kardex' ? <KardexScreen key={selectedIngredient} store={store} name={selectedIngredient} unit={unitOf || ''} onBack={()=>setView('stock')} />
        : view === 'ingreso' ? <IngresoForm store={store} name={selectedIngredient} setName={setSelectedIngredient} notify={notify} />
        : view === 'conteo' ? <ConteoForm store={store} name={selectedIngredient} setName={setSelectedIngredient} notify={notify} /> : <>
        <MermaForm store={store} name={selectedIngredient} setName={setSelectedIngredient} notify={notify} />
        <div className="turn-summary waste-auto"><h3>Mermas por platillo anulado (turno)</h3>
          {mermas.length === 0 ? <p className="muted">Aún no hay anulaciones con merma. Si cocina o el mozo anulan un platillo ya preparado, aparece aquí.</p> : <div className="simple-list">{mermas.map(w => <div key={w.id}><span>M{w.table}</span><b>{w.qty}× {w.dish}<small className="reason-small"> · Plato anulado · {w.reason}</small></b><strong className="bad">Merma</strong></div>)}</div>}
        </div>
      </>}
    </main>
    <BottomNav active={view === 'kardex' ? 'stock' : view} onChange={setView} items={[{id:'stock',label:'Stock',icon:'box'},{id:'ingreso',label:'Ingreso',icon:'plus'},{id:'conteo',label:'Conteo',icon:'check'},{id:'merma',label:'Merma',icon:'warning'}]} />
    {saved && <div className="toast"><Icon name="check"/>{saved}</div>}
  </div>
}

function InventoryDetail({ item, onKardex, onMove }: { item: Insumo; onKardex: () => void; onMove: () => void }) {
  const low = item.stock < item.min
  return <aside className="inventory-detail"><span className="ingredient-icon large"><Icon name="box"/></span><span className="eyebrow">DETALLE DEL INSUMO</span><h2>{item.name}</h2>{low && <div className="low-badge"><Icon name="warning" size={15}/> Bajo el mínimo</div>}<div className="stock-big"><span>Stock actual</span><strong className="number">{fmt(item.stock)} {item.unit}</strong></div><dl><div><dt>Stock mínimo</dt><dd className="number">{fmt(item.min)} {item.unit}</dd></div><div><dt>Valor actual</dt><dd className="money">{money(item.stock * item.cost)}</dd></div><div><dt>Última compra</dt><dd>{item.last}</dd></div></dl><button className="secondary full" onClick={onKardex}><Icon name="history" size={18}/> Ver Kardex completo</button><button className="primary full" onClick={onMove}><Icon name="plus" size={18}/> Registrar movimiento</button></aside>
}

function KardexScreen({ store, name, unit, onBack }: { store: Store; name: string; unit: string; onBack: () => void }) {
  const [filter, setFilter] = useState('Todos')
  const [period, setPeriod] = useState(30)
  const all = store.kardex[name] || []
  const moves = all.filter(m => (filter === 'Todos' || m.type === filter) && m.days <= period)
  const item = store.inv.find(i => i.name === name)
  return <div className="kardex-screen"><button className="back-link" onClick={onBack}>← Volver a inventario</button>
    <div className="kardex-title"><div><span className="eyebrow">HISTORIAL POR INSUMO</span><h2>Kardex · {name}</h2><p>Saldo actual: <b className="number">{fmt(item?.stock || 0)} {unit}</b></p></div>
      <div className="period-switch">{[{ l: 'Hoy', v: 0 }, { l: '7 días', v: 7 }, { l: '30 días', v: 30 }].map(p => <button className={period === p.v ? 'active' : ''} onClick={() => setPeriod(p.v)} key={p.v}>{p.l}</button>)}</div></div>
    <div className="kardex-filters">{['Todos','Compra','Consumo','Merma','Conteo'].map(f=><button className={filter===f?'active':''} onClick={()=>setFilter(f)} key={f}>{f}</button>)}</div>
    <div className="kardex-full"><div className="kardex-row head"><span>Movimiento</span><span>Cantidad</span><span>Saldo</span><span>Valor</span></div>
      {moves.length === 0 ? <div className="mini-empty"><Icon name="history" /><b>Sin movimientos</b><span>No hay registros con este filtro y periodo.</span></div> : moves.map(m=><div className="kardex-row" key={m.date+m.type+m.qty}><div><span className={`move-icon ${m.in?'in':'out'}`}>{m.in?'+':'−'}</span><span><b>{m.type}</b><small>{m.date} · {m.detail}</small></span></div><strong className={`number ${m.in?'positive':''}`}>{m.qty}</strong><span className="number">{m.balance}</span><span className="money">{money(m.value)}</span></div>)}</div></div>
}

function FormCard({ eyebrow, title, action, danger, disabled, onSave, children }: { eyebrow: string; title: string; action: string; danger?: boolean; disabled?: boolean; onSave: () => void; children: React.ReactNode }) {
  return <div className="form-wrap"><div className="form-intro"><span className="eyebrow">{eyebrow}</span><h2>{title}</h2><p>Completa la información para mantener el inventario actualizado.</p></div><div className="form-card">{children}<button className={danger ? 'danger-button full' : 'primary full'} disabled={disabled} onClick={onSave}>{action}<Icon name="arrow"/></button></div></div>
}

function ProfilePanel({ role, theme, setTheme, demoState, setDemoState, setRole, onLogout, onClose, onReset }: { onReset: () => void; role: Role; theme: ThemeMode; setTheme: (t: ThemeMode) => void; demoState: DemoState; setDemoState: (s: DemoState) => void; setRole: (r: Role) => void; onLogout: () => void; onClose: () => void }) {
  const emp = useContext(EmpCtx)
  return <div className="modal-backdrop align-right" onClick={onClose}><aside className="profile-panel" onClick={e=>e.stopPropagation()}>
    <div className="profile-panel-head"><Brand compact/><button className="icon-button" onClick={onClose}><Icon name="close"/></button></div>
    <div className="profile-identity"><div className="avatar large">{emp.initials}</div><div><h3>{emp.name}</h3><p>{roleLabel[role]} · Sede Central</p></div></div>
    <label>Apariencia</label><div className="theme-switch">{(['claro','oscuro','automatico'] as ThemeMode[]).map(t=><button className={theme===t?'active':''} onClick={()=>setTheme(t)} key={t}>{t === 'automatico' ? 'Automático' : t.charAt(0).toUpperCase()+t.slice(1)}</button>)}</div>
    <div className="profile-info"><span>Restaurante</span><b>GastroMind Sede Central</b><span>Turno</span><b>Hoy · 08:00 a 17:00</b></div>
    <details className="demo-menu"><summary>Modo demo</summary><p>Herramientas ocultas en producción.</p><label>Vista de rol</label><div className="role-switch">{(['mozo','cocinero','almacenero'] as Role[]).map(r=><button className={role===r?'active':''} onClick={()=>{setRole(r);onClose()}} key={r}>{roleLabel[r]}</button>)}</div><label>Simular estado</label><div className="demo-states">{(['contenido','cargando','vacio','error'] as DemoState[]).map(s=><button className={demoState===s?'active':''} onClick={()=>{setDemoState(s);onClose()}} key={s}>{s === 'vacio' ? 'Vacío' : s.charAt(0).toUpperCase()+s.slice(1)}</button>)}</div><button className="ghost reset-demo" onClick={() => { onReset(); onClose() }}>Restablecer datos de demostración</button></details>
    <button className="logout" onClick={onLogout}>Cerrar sesión <Icon name="arrow"/></button>
  </aside></div>
}

export default function App() {
  const [employee, setEmployee] = useState<Employee | null>(null)
  const [sessionEnded, setSessionEnded] = useState(false)
  const [demoState, setDemoState] = useState<DemoState>('contenido')
  const [theme, setTheme] = useState<ThemeMode>('claro')
  const [profile, setProfile] = useState(false)
  const store = useStore()
  useEffect(() => {
    const media = window.matchMedia('(prefers-color-scheme: dark)')
    const apply = () => { document.documentElement.dataset.theme = theme === 'automatico' ? (media.matches ? 'oscuro' : 'claro') : theme }
    apply()
    media.addEventListener('change', apply)
    return () => media.removeEventListener('change', apply)
  }, [theme])
  if (!employee) return <Login sessionEnded={sessionEnded} onLogin={e => { setEmployee(e); setSessionEnded(false) }} />
  const role = employee.role
  const props = { demoState, onProfile: () => setProfile(true), store }
  return <StoreCtx.Provider value={store}><EmpCtx.Provider value={employee}>
    {role === 'mozo' ? <Mozo {...props} /> : role === 'cocinero' ? <Cocina {...props} /> : <Almacen {...props} />}
    {profile && <ProfilePanel role={role} theme={theme} setTheme={setTheme} demoState={demoState} setDemoState={setDemoState} setRole={r => setEmployee(employees.find(e => e.role === r) || employee)} onReset={store.reset} onClose={()=>setProfile(false)} onLogout={()=>{setProfile(false);setEmployee(null);setSessionEnded(true)}}/>}
  </EmpCtx.Provider></StoreCtx.Provider>
}
