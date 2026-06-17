import { useState, useMemo } from "react";
import {
  Home, Users, UserPlus, Activity, BarChart2, Plus, Search, X, ChevronRight,
  RefreshCw, Repeat, Globe, Upload, Camera, Zap, Star, Bell, Settings,
  CheckCircle, Wallet, TrendingUp, Tag, Trash2, Shield, DollarSign,
  AlertCircle, ArrowUpRight, ArrowDownLeft
} from "lucide-react";
import {
  PieChart as RePie, Pie, Cell, Tooltip, BarChart, Bar, XAxis, YAxis,
  CartesianGrid, ResponsiveContainer
} from "recharts";

// ─── Types ────────────────────────────────────────────────────────────────────

type Friend = { id: string; name: string; email: string; avatarColor: string; currency: string };
type Group = { id: string; name: string; icon: string; color: string; memberIds: string[]; description: string };
type SplitType = "equal" | "custom" | "percent" | "shares";
type RecurringInterval = "weekly" | "monthly" | "yearly";
type Category = "Food" | "Travel" | "Home" | "Entertainment" | "Utilities" | "Shopping" | "Health" | "Other";
type Expense = {
  id: string; title: string; amount: number; currency: string; category: Category;
  paidById: string; splitType: SplitType; splits: Record<string, number>;
  date: string; groupId?: string; isRecurring: boolean; recurringInterval?: RecurringInterval;
};
type View = "home" | "groups" | "friends" | "activity" | "analytics";

// ─── Constants ────────────────────────────────────────────────────────────────

const ME: Friend = { id: "me", name: "You", email: "you@splitwise.app", avatarColor: "#1a237e", currency: "USD" };

const CURRENCIES = [
  { code: "USD", symbol: "$", name: "US Dollar" }, { code: "EUR", symbol: "€", name: "Euro" },
  { code: "GBP", symbol: "£", name: "British Pound" }, { code: "JPY", symbol: "¥", name: "Japanese Yen" },
  { code: "INR", symbol: "₹", name: "Indian Rupee" }, { code: "CAD", symbol: "CA$", name: "Canadian Dollar" },
  { code: "AUD", symbol: "A$", name: "Australian Dollar" }, { code: "CHF", symbol: "Fr", name: "Swiss Franc" },
  { code: "CNY", symbol: "¥", name: "Chinese Yuan" }, { code: "SGD", symbol: "S$", name: "Singapore Dollar" },
  { code: "HKD", symbol: "HK$", name: "Hong Kong Dollar" }, { code: "NZD", symbol: "NZ$", name: "New Zealand Dollar" },
  { code: "MXN", symbol: "MX$", name: "Mexican Peso" }, { code: "BRL", symbol: "R$", name: "Brazilian Real" },
  { code: "KRW", symbol: "₩", name: "South Korean Won" }, { code: "SEK", symbol: "kr", name: "Swedish Krona" },
  { code: "NOK", symbol: "kr", name: "Norwegian Krone" }, { code: "DKK", symbol: "kr", name: "Danish Krone" },
  { code: "PLN", symbol: "zł", name: "Polish Złoty" }, { code: "THB", symbol: "฿", name: "Thai Baht" },
  { code: "IDR", symbol: "Rp", name: "Indonesian Rupiah" }, { code: "HUF", symbol: "Ft", name: "Hungarian Forint" },
  { code: "CZK", symbol: "Kč", name: "Czech Koruna" }, { code: "ILS", symbol: "₪", name: "Israeli Shekel" },
  { code: "CLP", symbol: "CL$", name: "Chilean Peso" }, { code: "PHP", symbol: "₱", name: "Philippine Peso" },
  { code: "AED", symbol: "د.إ", name: "UAE Dirham" }, { code: "SAR", symbol: "ر.س", name: "Saudi Riyal" },
  { code: "MYR", symbol: "RM", name: "Malaysian Ringgit" }, { code: "RON", symbol: "lei", name: "Romanian Leu" },
  { code: "TRY", symbol: "₺", name: "Turkish Lira" }, { code: "ZAR", symbol: "R", name: "South African Rand" },
  { code: "RUB", symbol: "₽", name: "Russian Ruble" }, { code: "NGN", symbol: "₦", name: "Nigerian Naira" },
  { code: "KES", symbol: "KSh", name: "Kenyan Shilling" }, { code: "GHS", symbol: "₵", name: "Ghanaian Cedi" },
  { code: "EGP", symbol: "£", name: "Egyptian Pound" }, { code: "PKR", symbol: "₨", name: "Pakistani Rupee" },
  { code: "BDT", symbol: "৳", name: "Bangladeshi Taka" }, { code: "VND", symbol: "₫", name: "Vietnamese Dong" },
  { code: "UAH", symbol: "₴", name: "Ukrainian Hryvnia" }, { code: "COP", symbol: "CO$", name: "Colombian Peso" },
  { code: "PEN", symbol: "S/", name: "Peruvian Sol" }, { code: "ARS", symbol: "AR$", name: "Argentine Peso" },
  { code: "TWD", symbol: "NT$", name: "Taiwan Dollar" }, { code: "QAR", symbol: "ر.ق", name: "Qatari Rial" },
  { code: "KWD", symbol: "د.ك", name: "Kuwaiti Dinar" }, { code: "OMR", symbol: "ر.ع.", name: "Omani Rial" },
  { code: "BHD", symbol: ".د.ب", name: "Bahraini Dinar" }, { code: "JOD", symbol: "JD", name: "Jordanian Dinar" },
  { code: "LKR", symbol: "Rs", name: "Sri Lankan Rupee" }, { code: "NPR", symbol: "रू", name: "Nepalese Rupee" },
  { code: "MMK", symbol: "K", name: "Myanmar Kyat" }, { code: "KHR", symbol: "៛", name: "Cambodian Riel" },
  { code: "LAK", symbol: "₭", name: "Lao Kip" }, { code: "MVR", symbol: "Rf", name: "Maldivian Rufiyaa" },
  { code: "AFN", symbol: "؋", name: "Afghan Afghani" }, { code: "AMD", symbol: "֏", name: "Armenian Dram" },
  { code: "AZN", symbol: "₼", name: "Azerbaijani Manat" }, { code: "BYN", symbol: "Br", name: "Belarusian Ruble" },
  { code: "GEL", symbol: "₾", name: "Georgian Lari" }, { code: "KZT", symbol: "₸", name: "Kazakhstani Tenge" },
  { code: "KGS", symbol: "с", name: "Kyrgystani Som" }, { code: "TJS", symbol: "SM", name: "Tajikistani Somoni" },
  { code: "TMT", symbol: "T", name: "Turkmenistani Manat" }, { code: "UZS", symbol: "so'm", name: "Uzbekistani Som" },
  { code: "MNT", symbol: "₮", name: "Mongolian Tugrik" }, { code: "BOB", symbol: "Bs.", name: "Bolivian Boliviano" },
  { code: "GTQ", symbol: "Q", name: "Guatemalan Quetzal" }, { code: "HNL", symbol: "L", name: "Honduran Lempira" },
  { code: "NIO", symbol: "C$", name: "Nicaraguan Córdoba" }, { code: "PAB", symbol: "B/.", name: "Panamanian Balboa" },
  { code: "DOP", symbol: "RD$", name: "Dominican Peso" }, { code: "JMD", symbol: "J$", name: "Jamaican Dollar" },
  { code: "TTD", symbol: "TT$", name: "Trinidad & Tobago Dollar" }, { code: "BBD", symbol: "BBD$", name: "Barbadian Dollar" },
  { code: "BSD", symbol: "BS$", name: "Bahamian Dollar" }, { code: "BZD", symbol: "BZ$", name: "Belize Dollar" },
  { code: "HTG", symbol: "G", name: "Haitian Gourde" }, { code: "XCD", symbol: "EC$", name: "East Caribbean Dollar" },
  { code: "UYU", symbol: "$U", name: "Uruguayan Peso" }, { code: "PYG", symbol: "₲", name: "Paraguayan Guaraní" },
  { code: "VES", symbol: "Bs.S", name: "Venezuelan Bolívar" }, { code: "GYD", symbol: "G$", name: "Guyanese Dollar" },
  { code: "SRD", symbol: "SRD$", name: "Surinamese Dollar" }, { code: "FJD", symbol: "FJ$", name: "Fijian Dollar" },
  { code: "PGK", symbol: "K", name: "Papua New Guinean Kina" }, { code: "SBD", symbol: "SI$", name: "Solomon Islands Dollar" },
  { code: "TOP", symbol: "T$", name: "Tongan Paʻanga" }, { code: "WST", symbol: "WS$", name: "Samoan Tālā" },
  { code: "VUV", symbol: "VT", name: "Vanuatu Vatu" }, { code: "XPF", symbol: "Fr", name: "CFP Franc" },
  { code: "ETB", symbol: "Br", name: "Ethiopian Birr" }, { code: "TZS", symbol: "TSh", name: "Tanzanian Shilling" },
  { code: "UGX", symbol: "USh", name: "Ugandan Shilling" }, { code: "RWF", symbol: "FRw", name: "Rwandan Franc" },
  { code: "XOF", symbol: "CFA", name: "West African CFA Franc" }, { code: "XAF", symbol: "FCFA", name: "Central African CFA Franc" },
  { code: "MZN", symbol: "MT", name: "Mozambican Metical" }, { code: "ZMW", symbol: "ZK", name: "Zambian Kwacha" },
];

const EXCHANGE_RATES: Record<string, number> = {
  USD: 1, EUR: 0.92, GBP: 0.79, JPY: 149.5, INR: 83.2, CAD: 1.36,
  AUD: 1.53, CHF: 0.89, CNY: 7.24, SGD: 1.34, HKD: 7.82, NZD: 1.63,
  MXN: 17.1, BRL: 4.97, KRW: 1325, SEK: 10.4, NOK: 10.6, DKK: 6.88,
  PLN: 4.03, THB: 35.6, IDR: 15680, HUF: 358, CZK: 22.9, ILS: 3.69,
  CLP: 910, PHP: 56.3, AED: 3.67, SAR: 3.75, MYR: 4.71, RON: 4.57,
  TRY: 30.7, ZAR: 18.9, RUB: 90.2, NGN: 1580, KES: 154, GHS: 12.4,
  EGP: 30.9, PKR: 279, BDT: 110, VND: 24400, UAH: 38.4, COP: 3980,
  PEN: 3.72, ARS: 840,
};

const CATEGORIES: { name: Category; icon: string; color: string }[] = [
  { name: "Food", icon: "🍔", color: "#ff8f00" },
  { name: "Travel", icon: "✈️", color: "#1a237e" },
  { name: "Home", icon: "🏠", color: "#00bfa5" },
  { name: "Entertainment", icon: "🎬", color: "#7c3aed" },
  { name: "Utilities", icon: "💡", color: "#0891b2" },
  { name: "Shopping", icon: "🛍️", color: "#db2777" },
  { name: "Health", icon: "💊", color: "#16a34a" },
  { name: "Other", icon: "📦", color: "#6b7280" },
];

const AVATAR_COLORS = ["#1a237e", "#00bfa5", "#ff8f00", "#7c3aed", "#db2777", "#0891b2", "#16a34a", "#ff5252"];
const GROUP_ICONS = ["✈️", "🏠", "🍔", "🎉", "💼", "🏋️", "🎮", "🎵", "📚", "⚽"];
const GROUP_COLORS = ["#1a237e", "#00bfa5", "#ff8f00", "#7c3aed", "#db2777", "#0891b2", "#16a34a"];
const LANGUAGES = ["English", "Hindi", "Spanish", "French", "German", "Japanese", "Chinese", "Portuguese", "Arabic", "Korean"];

// ─── Seed Data ────────────────────────────────────────────────────────────────

const SEED_FRIENDS: Friend[] = [
  { id: "f1", name: "Priya Sharma", email: "priya@example.com", avatarColor: "#db2777", currency: "INR" },
  { id: "f2", name: "Marcus Webb", email: "marcus@example.com", avatarColor: "#00bfa5", currency: "GBP" },
  { id: "f3", name: "Yuki Tanaka", email: "yuki@example.com", avatarColor: "#7c3aed", currency: "JPY" },
];

const SEED_GROUPS: Group[] = [
  { id: "g1", name: "Bali Trip 2025", icon: "✈️", color: "#1a237e", memberIds: ["me", "f1", "f2", "f3"], description: "10-day Bali adventure" },
  { id: "g2", name: "Flat 4B", icon: "🏠", color: "#00bfa5", memberIds: ["me", "f1", "f2"], description: "Monthly household expenses" },
  { id: "g3", name: "Office Lunch", icon: "🍔", color: "#ff8f00", memberIds: ["me", "f2", "f3"], description: "Weekday lunch crew" },
];

const SEED_EXPENSES: Expense[] = [
  { id: "e1", title: "Villa Seminyak", amount: 1200, currency: "USD", category: "Travel", paidById: "me", splitType: "equal", splits: { me: 300, f1: 300, f2: 300, f3: 300 }, date: "2025-06-01", groupId: "g1", isRecurring: false },
  { id: "e2", title: "Airport Transfer", amount: 80, currency: "USD", category: "Travel", paidById: "f1", splitType: "equal", splits: { me: 20, f1: 20, f2: 20, f3: 20 }, date: "2025-06-01", groupId: "g1", isRecurring: false },
  { id: "e3", title: "Rent — June", amount: 2400, currency: "USD", category: "Home", paidById: "me", splitType: "equal", splits: { me: 800, f1: 800, f2: 800 }, date: "2025-06-01", groupId: "g2", isRecurring: true, recurringInterval: "monthly" },
  { id: "e4", title: "Internet Bill", amount: 90, currency: "USD", category: "Utilities", paidById: "f2", splitType: "equal", splits: { me: 30, f1: 30, f2: 30 }, date: "2025-06-05", groupId: "g2", isRecurring: true, recurringInterval: "monthly" },
  { id: "e5", title: "Sushi Lunch", amount: 145, currency: "USD", category: "Food", paidById: "me", splitType: "equal", splits: { me: 48.33, f2: 48.33, f3: 48.34 }, date: "2025-06-10", groupId: "g3", isRecurring: false },
  { id: "e6", title: "Movie Night", amount: 60, currency: "USD", category: "Entertainment", paidById: "f3", splitType: "percent", splits: { me: 15, f2: 22.5, f3: 22.5 }, date: "2025-06-12", isRecurring: false },
  { id: "e7", title: "Groceries", amount: 210, currency: "USD", category: "Shopping", paidById: "f1", splitType: "custom", splits: { me: 70, f1: 70, f2: 70 }, date: "2025-06-14", groupId: "g2", isRecurring: false },
  { id: "e8", title: "Thai Dinner", amount: 95, currency: "USD", category: "Food", paidById: "f2", splitType: "equal", splits: { me: 31.67, f2: 31.67, f3: 31.67 }, date: "2025-06-15", groupId: "g3", isRecurring: false },
  { id: "e9", title: "Gym Membership", amount: 120, currency: "USD", category: "Health", paidById: "me", splitType: "equal", splits: { me: 60, f2: 60 }, date: "2025-06-01", isRecurring: true, recurringInterval: "monthly" },
  { id: "e10", title: "Cooking Class", amount: 200, currency: "USD", category: "Entertainment", paidById: "f1", splitType: "shares", splits: { me: 66.67, f1: 66.67, f3: 66.67 }, date: "2025-06-08", groupId: "g1", isRecurring: false },
];

// ─── Helpers ──────────────────────────────────────────────────────────────────

function toUSD(amount: number, currency: string): number {
  return amount / (EXCHANGE_RATES[currency] ?? 1);
}

function formatAmt(amount: number, currency = "USD"): string {
  const curr = CURRENCIES.find(c => c.code === currency);
  return `${curr?.symbol ?? "$"}${Math.abs(amount).toFixed(2)}`;
}

function initials(name: string): string {
  return name.split(" ").map(n => n[0]).join("").toUpperCase().slice(0, 2);
}

function getCatMeta(cat: Category) {
  return CATEGORIES.find(c => c.name === cat) ?? CATEGORIES[7];
}

function computeBalances(expenses: Expense[], friends: Friend[]): Record<string, number> {
  const balances: Record<string, number> = {};
  [ME, ...friends].forEach(p => { balances[p.id] = 0; });
  expenses.forEach(exp => {
    const total = toUSD(exp.amount, exp.currency);
    Object.entries(exp.splits).forEach(([pid, share]) => {
      const shareUSD = toUSD(share, exp.currency);
      if (pid === exp.paidById) {
        balances[pid] = (balances[pid] ?? 0) + (total - shareUSD);
      } else {
        balances[pid] = (balances[pid] ?? 0) - shareUSD;
      }
    });
  });
  return balances;
}

type SimplifiedDebt = { from: string; to: string; amount: number };
function simplifyDebts(balances: Record<string, number>, friends: Friend[]): SimplifiedDebt[] {
  const all = [ME, ...friends];
  const cr = all.filter(p => (balances[p.id] ?? 0) > 0.01).map(p => ({ id: p.id, bal: balances[p.id] })).sort((a, b) => b.bal - a.bal);
  const dr = all.filter(p => (balances[p.id] ?? 0) < -0.01).map(p => ({ id: p.id, bal: balances[p.id] })).sort((a, b) => a.bal - b.bal);
  const result: SimplifiedDebt[] = [];
  let ci = 0, di = 0;
  while (ci < cr.length && di < dr.length) {
    const settle = Math.min(cr[ci].bal, -dr[di].bal);
    result.push({ from: dr[di].id, to: cr[ci].id, amount: settle });
    cr[ci].bal -= settle;
    dr[di].bal += settle;
    if (cr[ci].bal < 0.01) ci++;
    if (dr[di].bal > -0.01) di++;
  }
  return result;
}

function friendName(id: string, friends: Friend[]): string {
  if (id === "me") return "You";
  return friends.find(f => f.id === id)?.name ?? id;
}

function friendById(id: string, friends: Friend[]): Friend {
  if (id === "me") return ME;
  return friends.find(f => f.id === id) ?? ME;
}

// ─── Shared UI ────────────────────────────────────────────────────────────────

function Avatar({ person, size = 36 }: { person: Friend; size?: number }) {
  return (
    <div className="rounded-full flex items-center justify-center text-white font-bold shrink-0"
      style={{ width: size, height: size, background: person.avatarColor, fontSize: size * 0.36 }}>
      {initials(person.name)}
    </div>
  );
}

function CategoryBadge({ category }: { category: Category }) {
  const m = getCatMeta(category);
  return (
    <span className="inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full font-semibold"
      style={{ background: m.color + "18", color: m.color }}>
      {m.icon} {category}
    </span>
  );
}

function Modal({ open, onClose, title, children }: { open: boolean; onClose: () => void; title: string; children: React.ReactNode }) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center" style={{ background: "rgba(0,0,0,0.45)" }} onClick={onClose}>
      <div className="bg-card w-full max-w-md rounded-t-3xl shadow-2xl" style={{ maxHeight: "88vh", overflowY: "auto" }}
        onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between px-5 py-4 border-b border-border sticky top-0 bg-card z-10">
          <h2 className="text-base font-bold">{title}</h2>
          <button onClick={onClose} className="p-1.5 rounded-full hover:bg-muted transition-colors"><X size={18} /></button>
        </div>
        <div className="p-5">{children}</div>
      </div>
    </div>
  );
}

const INPUT = "w-full bg-secondary border border-border rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20";
const LABEL = "block text-xs font-bold text-muted-foreground mb-1.5 uppercase tracking-wider";

// ─── Add Expense Modal ────────────────────────────────────────────────────────

function AddExpenseModal({ open, onClose, friends, groups, onAdd }: {
  open: boolean; onClose: () => void; friends: Friend[]; groups: Group[]; onAdd: (e: Expense) => void;
}) {
  const [title, setTitle] = useState("");
  const [amount, setAmount] = useState("");
  const [currency, setCurrency] = useState("USD");
  const [category, setCategory] = useState<Category>("Food");
  const [paidById, setPaidById] = useState("me");
  const [groupId, setGroupId] = useState("");
  const [splitType, setSplitType] = useState<SplitType>("equal");
  const [members, setMembers] = useState<string[]>(["me"]);
  const [customSplits, setCustomSplits] = useState<Record<string, string>>({});
  const [isRecurring, setIsRecurring] = useState(false);
  const [interval, setInterval] = useState<RecurringInterval>("monthly");
  const [scanning, setScanning] = useState(false);

  const allPeople = [ME, ...friends];

  const handleScan = () => {
    setScanning(true);
    setTimeout(() => { setTitle("Restaurant Bill"); setAmount("87.50"); setCategory("Food"); setScanning(false); }, 2000);
  };

  const toggle = (id: string) => setMembers(prev => prev.includes(id) ? (prev.length > 1 ? prev.filter(x => x !== id) : prev) : [...prev, id]);

  const submit = () => {
    if (!title.trim() || !amount) return;
    const total = parseFloat(amount);
    const splits: Record<string, number> = {};
    if (splitType === "equal") {
      const share = parseFloat((total / members.length).toFixed(2));
      members.forEach(id => { splits[id] = share; });
    } else {
      members.forEach(id => { splits[id] = parseFloat(customSplits[id] ?? "0"); });
    }
    onAdd({ id: Date.now().toString(), title, amount: total, currency, category, paidById, splitType, splits, date: new Date().toISOString().split("T")[0], groupId: groupId || undefined, isRecurring, recurringInterval: isRecurring ? interval : undefined });
    onClose();
    setTitle(""); setAmount(""); setCurrency("USD"); setCategory("Food"); setPaidById("me"); setGroupId(""); setSplitType("equal"); setMembers(["me"]); setCustomSplits({}); setIsRecurring(false);
  };

  return (
    <Modal open={open} onClose={onClose} title="Add Expense">
      <div className="space-y-4">
        <button onClick={handleScan} className="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl border-2 border-dashed border-accent/40 text-accent text-sm font-bold hover:bg-accent/5 transition-colors">
          {scanning ? <><RefreshCw size={15} className="animate-spin" /> Scanning receipt…</> : <><Camera size={15} /> Scan Receipt (OCR)</>}
        </button>

        <div>
          <label className={LABEL}>Description</label>
          <input className={INPUT} placeholder="e.g. Dinner at Nobu" value={title} onChange={e => setTitle(e.target.value)} />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div><label className={LABEL}>Amount</label><input className={INPUT} type="number" placeholder="0.00" value={amount} onChange={e => setAmount(e.target.value)} /></div>
          <div><label className={LABEL}>Currency</label>
            <select className={INPUT} value={currency} onChange={e => setCurrency(e.target.value)}>
              {CURRENCIES.map(c => <option key={c.code} value={c.code}>{c.code} {c.symbol}</option>)}
            </select>
          </div>
        </div>

        <div>
          <label className={LABEL}>Category</label>
          <div className="flex flex-wrap gap-2">
            {CATEGORIES.map(cat => (
              <button key={cat.name} onClick={() => setCategory(cat.name)}
                className="flex items-center gap-1 px-3 py-1.5 rounded-full text-xs font-bold transition-all"
                style={{ background: category === cat.name ? cat.color : cat.color + "15", color: category === cat.name ? "#fff" : cat.color }}>
                {cat.icon} {cat.name}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label className={LABEL}>Paid by</label>
          <div className="flex flex-wrap gap-2">
            {allPeople.map(p => (
              <button key={p.id} onClick={() => setPaidById(p.id)}
                className="flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-bold transition-all border"
                style={{ background: paidById === p.id ? p.avatarColor : "transparent", color: paidById === p.id ? "#fff" : p.avatarColor, borderColor: p.avatarColor + "50" }}>
                {initials(p.name)} {p.id === "me" ? "You" : p.name.split(" ")[0]}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label className={LABEL}>Group (optional)</label>
          <select className={INPUT} value={groupId} onChange={e => setGroupId(e.target.value)}>
            <option value="">No group</option>
            {groups.map(g => <option key={g.id} value={g.id}>{g.icon} {g.name}</option>)}
          </select>
        </div>

        <div>
          <label className={LABEL}>Split with</label>
          <div className="flex flex-wrap gap-2">
            {allPeople.map(p => (
              <button key={p.id} onClick={() => toggle(p.id)}
                className="flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-bold transition-all border"
                style={{ background: members.includes(p.id) ? p.avatarColor : "transparent", color: members.includes(p.id) ? "#fff" : p.avatarColor, borderColor: p.avatarColor + "50" }}>
                {initials(p.name)} {p.id === "me" ? "You" : p.name.split(" ")[0]}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label className={LABEL}>Split method</label>
          <div className="grid grid-cols-4 gap-1 bg-muted p-1 rounded-xl">
            {(["equal", "custom", "percent", "shares"] as SplitType[]).map(t => (
              <button key={t} onClick={() => setSplitType(t)}
                className="py-1.5 rounded-lg text-xs font-bold capitalize transition-all"
                style={{ background: splitType === t ? "#1a237e" : "transparent", color: splitType === t ? "#fff" : "#6b7280" }}>
                {t}
              </button>
            ))}
          </div>
        </div>

        {splitType !== "equal" && (
          <div>
            <label className={LABEL}>{splitType === "percent" ? "Percentages" : splitType === "shares" ? "Shares" : "Amounts"}</label>
            <div className="space-y-2">
              {members.map(id => {
                const p = friendById(id, friends);
                return (
                  <div key={id} className="flex items-center gap-3">
                    <Avatar person={p} size={28} />
                    <span className="text-sm flex-1 font-medium">{id === "me" ? "You" : p.name.split(" ")[0]}</span>
                    <div className="flex items-center gap-1">
                      <input className="w-20 bg-secondary border border-border rounded-lg px-2 py-1.5 text-sm text-right font-mono" type="number" placeholder="0"
                        value={customSplits[id] ?? ""} onChange={e => setCustomSplits(prev => ({ ...prev, [id]: e.target.value }))} />
                      <span className="text-xs text-muted-foreground">{splitType === "percent" ? "%" : splitType === "shares" ? "sh" : currency}</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        <div className="flex items-center justify-between py-2 border-t border-border">
          <div className="flex items-center gap-2"><Repeat size={15} className="text-muted-foreground" /><span className="text-sm font-medium">Recurring expense</span></div>
          <button onClick={() => setIsRecurring(!isRecurring)} style={{ width: 40, height: 22, background: isRecurring ? "#00bfa5" : "#c8ccd6", borderRadius: 11, position: "relative", transition: "background .2s", flexShrink: 0 }}>
            <span style={{ position: "absolute", top: 3, left: isRecurring ? 21 : 3, width: 16, height: 16, background: "#fff", borderRadius: "50%", transition: "left .2s", boxShadow: "0 1px 3px rgba(0,0,0,.2)" }} />
          </button>
        </div>

        {isRecurring && (
          <div className="grid grid-cols-3 gap-1 bg-muted p-1 rounded-xl">
            {(["weekly", "monthly", "yearly"] as RecurringInterval[]).map(t => (
              <button key={t} onClick={() => setInterval(t)}
                className="py-1.5 rounded-lg text-xs font-bold capitalize transition-all"
                style={{ background: interval === t ? "#1a237e" : "transparent", color: interval === t ? "#fff" : "#6b7280" }}>
                {t}
              </button>
            ))}
          </div>
        )}

        <button onClick={submit} className="w-full py-3 rounded-xl font-bold text-white text-sm hover:opacity-90 active:scale-95 transition-all"
          style={{ background: "linear-gradient(135deg,#1a237e,#3949ab)" }}>
          Add Expense
        </button>
      </div>
    </Modal>
  );
}

// ─── Add Group Modal ──────────────────────────────────────────────────────────

function AddGroupModal({ open, onClose, friends, onAdd }: { open: boolean; onClose: () => void; friends: Friend[]; onAdd: (g: Group) => void }) {
  const [name, setName] = useState("");
  const [icon, setIcon] = useState("✈️");
  const [color, setColor] = useState("#1a237e");
  const [desc, setDesc] = useState("");
  const [mids, setMids] = useState<string[]>(["me"]);
  const toggle = (id: string) => setMids(prev => prev.includes(id) ? (prev.length > 1 ? prev.filter(x => x !== id) : prev) : [...prev, id]);
  const submit = () => {
    if (!name.trim()) return;
    onAdd({ id: Date.now().toString(), name, icon, color, description: desc, memberIds: mids });
    onClose(); setName(""); setIcon("✈️"); setColor("#1a237e"); setDesc(""); setMids(["me"]);
  };
  return (
    <Modal open={open} onClose={onClose} title="Create Group">
      <div className="space-y-4">
        <div><label className={LABEL}>Group Name</label><input className={INPUT} placeholder="e.g. Weekend Getaway" value={name} onChange={e => setName(e.target.value)} /></div>
        <div><label className={LABEL}>Description</label><input className={INPUT} placeholder="What is this group for?" value={desc} onChange={e => setDesc(e.target.value)} /></div>
        <div>
          <label className={LABEL}>Icon</label>
          <div className="flex flex-wrap gap-2">
            {GROUP_ICONS.map(i => <button key={i} onClick={() => setIcon(i)} className="w-10 h-10 rounded-xl text-xl flex items-center justify-center transition-all border-2" style={{ borderColor: icon === i ? "#1a237e" : "transparent", background: icon === i ? "#1a237e10" : "#f4f6fb" }}>{i}</button>)}
          </div>
        </div>
        <div>
          <label className={LABEL}>Color</label>
          <div className="flex gap-2">{GROUP_COLORS.map(c => <button key={c} onClick={() => setColor(c)} className="w-8 h-8 rounded-full border-4 transition-all" style={{ background: c, borderColor: color === c ? "#0d1117" : "transparent" }} />)}</div>
        </div>
        <div>
          <label className={LABEL}>Members</label>
          <div className="flex flex-wrap gap-2">
            {[ME, ...friends].map(p => (
              <button key={p.id} onClick={() => toggle(p.id)}
                className="flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-bold transition-all border"
                style={{ background: mids.includes(p.id) ? p.avatarColor : "transparent", color: mids.includes(p.id) ? "#fff" : p.avatarColor, borderColor: p.avatarColor + "50" }}>
                {initials(p.name)} {p.id === "me" ? "You" : p.name.split(" ")[0]}
              </button>
            ))}
          </div>
        </div>
        <button onClick={submit} className="w-full py-3 rounded-xl font-bold text-white text-sm hover:opacity-90" style={{ background: "linear-gradient(135deg,#1a237e,#3949ab)" }}>Create Group</button>
      </div>
    </Modal>
  );
}

// ─── Add Friend Modal ─────────────────────────────────────────────────────────

function AddFriendModal({ open, onClose, onAdd }: { open: boolean; onClose: () => void; onAdd: (f: Friend) => void }) {
  const [name, setName] = useState(""); const [email, setEmail] = useState(""); const [ac, setAc] = useState(AVATAR_COLORS[1]); const [cur, setCur] = useState("USD");
  const submit = () => {
    if (!name.trim()) return;
    onAdd({ id: Date.now().toString(), name, email, avatarColor: ac, currency: cur });
    onClose(); setName(""); setEmail(""); setAc(AVATAR_COLORS[1]); setCur("USD");
  };
  return (
    <Modal open={open} onClose={onClose} title="Add Friend">
      <div className="space-y-4">
        <div className="flex justify-center">
          <div className="w-16 h-16 rounded-full flex items-center justify-center text-white text-2xl font-bold" style={{ background: ac }}>{name ? initials(name) : "?"}</div>
        </div>
        <div><label className={LABEL}>Name</label><input className={INPUT} placeholder="Full name" value={name} onChange={e => setName(e.target.value)} /></div>
        <div><label className={LABEL}>Email</label><input className={INPUT} type="email" placeholder="friend@example.com" value={email} onChange={e => setEmail(e.target.value)} /></div>
        <div><label className={LABEL}>Avatar Color</label><div className="flex gap-2">{AVATAR_COLORS.map(c => <button key={c} onClick={() => setAc(c)} className="w-8 h-8 rounded-full border-4 transition-all" style={{ background: c, borderColor: ac === c ? "#0d1117" : "transparent" }} />)}</div></div>
        <div><label className={LABEL}>Currency</label><select className={INPUT} value={cur} onChange={e => setCur(e.target.value)}>{CURRENCIES.map(c => <option key={c.code} value={c.code}>{c.code} — {c.name}</option>)}</select></div>
        <button onClick={submit} className="w-full py-3 rounded-xl font-bold text-white text-sm hover:opacity-90" style={{ background: "linear-gradient(135deg,#1a237e,#3949ab)" }}>Add Friend</button>
      </div>
    </Modal>
  );
}

// ─── Debt Simplification Modal ────────────────────────────────────────────────

function DebtModal({ open, onClose, balances, friends }: { open: boolean; onClose: () => void; balances: Record<string, number>; friends: Friend[] }) {
  const debts = simplifyDebts(balances, friends);
  return (
    <Modal open={open} onClose={onClose} title="Simplified Debts">
      <div className="space-y-3">
        <p className="text-xs text-muted-foreground">{debts.length === 0 ? "All settled!" : `Minimum ${debts.length} transaction${debts.length > 1 ? "s" : ""} to settle all debts.`}</p>
        {debts.length === 0 ? (
          <div className="flex flex-col items-center gap-3 py-8 text-center">
            <CheckCircle size={44} style={{ color: "#00bfa5" }} />
            <p className="font-bold text-lg">All settled up!</p>
          </div>
        ) : debts.map((d, i) => {
          const from = friendById(d.from, friends); const to = friendById(d.to, friends);
          return (
            <div key={i} className="flex items-center gap-3 p-3 rounded-xl bg-secondary">
              <Avatar person={from} size={32} />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-bold">{friendName(d.from, friends)}</p>
                <p className="text-xs text-muted-foreground">pays {friendName(d.to, friends)}</p>
              </div>
              <div className="flex flex-col items-end gap-1.5">
                <span className="font-mono font-bold text-sm" style={{ color: "#ff5252" }}>{formatAmt(d.amount)}</span>
                <div className="flex gap-1">
                  {["UPI", "PayPal", "Venmo"].map(p => (
                    <button key={p} className="text-xs px-2 py-0.5 rounded-full border border-primary/30 text-primary font-bold hover:bg-primary/10 transition-colors">{p}</button>
                  ))}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </Modal>
  );
}

// ─── Home Screen ──────────────────────────────────────────────────────────────

function HomeScreen({ expenses, friends, groups, balances, onDebt, onAdd }: {
  expenses: Expense[]; friends: Friend[]; groups: Group[]; balances: Record<string, number>; onDebt: () => void; onAdd: () => void;
}) {
  const myBal = balances["me"] ?? 0;
  const oweTotal = friends.filter(f => (balances[f.id] ?? 0) > 0.01).reduce((s, f) => s + (balances[f.id] ?? 0), 0);
  const owedTotal = friends.filter(f => (balances[f.id] ?? 0) < -0.01).reduce((s, f) => s + Math.abs(balances[f.id] ?? 0), 0);
  const recent = [...expenses].sort((a, b) => b.date.localeCompare(a.date)).slice(0, 5);

  return (
    <div className="space-y-4">
      {/* Balance Hero */}
      <div className="rounded-2xl p-5 text-white overflow-hidden relative" style={{ background: "linear-gradient(135deg,#1a237e 0%,#3949ab 60%,#5c6bc0 100%)" }}>
        <div className="absolute -top-8 -right-8 w-32 h-32 rounded-full opacity-10" style={{ background: "#fff" }} />
        <div className="absolute -bottom-6 -left-6 w-24 h-24 rounded-full opacity-10" style={{ background: "#00bfa5" }} />
        <p className="text-xs font-bold opacity-60 uppercase tracking-widest mb-1">Net Balance</p>
        <p className="text-4xl font-bold font-mono mb-3" style={{ color: myBal >= 0 ? "#a7f3d0" : "#fca5a5" }}>
          {myBal >= 0 ? "+" : ""}{formatAmt(myBal)}
        </p>
        <div className="flex gap-5">
          <div><p className="text-xs opacity-60">You owe</p><p className="text-sm font-bold font-mono" style={{ color: "#fca5a5" }}>{formatAmt(oweTotal)}</p></div>
          <div><p className="text-xs opacity-60">Owed to you</p><p className="text-sm font-bold font-mono" style={{ color: "#a7f3d0" }}>{formatAmt(owedTotal)}</p></div>
        </div>
      </div>

      {/* Quick actions */}
      <div className="grid grid-cols-3 gap-2.5">
        {[
          { label: "Add Expense", icon: <Plus size={18} />, color: "#1a237e", action: onAdd },
          { label: "Settle Up", icon: <Zap size={18} />, color: "#00bfa5", action: onDebt },
          { label: "Scan Receipt", icon: <Camera size={18} />, color: "#ff8f00", action: onAdd },
        ].map(a => (
          <button key={a.label} onClick={a.action} className="flex flex-col items-center gap-2 p-3 rounded-2xl bg-card border border-border hover:shadow-md transition-all active:scale-95">
            <div className="w-9 h-9 rounded-xl flex items-center justify-center text-white" style={{ background: a.color }}>{a.icon}</div>
            <span className="text-xs font-bold text-foreground">{a.label}</span>
          </button>
        ))}
      </div>

      {/* Friend Balances */}
      <div className="bg-card rounded-2xl border border-border p-4">
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-bold">Friend Balances</h3>
          <button onClick={onDebt} className="text-xs font-bold" style={{ color: "#1a237e" }}>Simplify →</button>
        </div>
        <div className="space-y-3">
          {friends.map(f => {
            const b = balances[f.id] ?? 0;
            return (
              <div key={f.id} className="flex items-center gap-3">
                <Avatar person={f} size={36} />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-bold truncate">{f.name}</p>
                  <p className="text-xs text-muted-foreground">{b === 0 ? "Settled up" : b > 0 ? "owes you" : "you owe"}</p>
                </div>
                {b !== 0 ? (
                  <span className="font-mono font-bold text-sm flex items-center gap-1" style={{ color: b < 0 ? "#ff5252" : "#00bfa5" }}>
                    {b < 0 ? <ArrowDownLeft size={13} /> : <ArrowUpRight size={13} />}{formatAmt(Math.abs(b))}
                  </span>
                ) : (
                  <span className="text-xs font-bold" style={{ color: "#00bfa5" }}>✓ Clear</span>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Recent Activity */}
      <div className="bg-card rounded-2xl border border-border p-4">
        <h3 className="text-sm font-bold mb-3">Recent Activity</h3>
        <div className="space-y-3">
          {recent.map(exp => {
            const cat = getCatMeta(exp.category);
            const myShare = exp.splits["me"] ?? 0;
            return (
              <div key={exp.id} className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl flex items-center justify-center text-base shrink-0" style={{ background: cat.color + "15" }}>{cat.icon}</div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-1.5">
                    <p className="text-sm font-bold truncate">{exp.title}</p>
                    {exp.isRecurring && <Repeat size={11} style={{ color: "#00bfa5" }} />}
                  </div>
                  <p className="text-xs text-muted-foreground">{exp.date} · {exp.paidById === "me" ? "You paid" : `${friendName(exp.paidById, friends)} paid`}</p>
                </div>
                <div className="text-right shrink-0">
                  <p className="text-sm font-bold font-mono">{formatAmt(exp.amount, exp.currency)}</p>
                  <p className="text-xs text-muted-foreground font-mono">you: {formatAmt(myShare, exp.currency)}</p>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Premium banner */}
      <div className="rounded-2xl p-4 flex items-center gap-3" style={{ background: "linear-gradient(135deg,#fef9ee,#fffbf0)", border: "1px solid #f59e0b30" }}>
        <div className="w-9 h-9 rounded-xl flex items-center justify-center" style={{ background: "#f59e0b20" }}><Star size={18} style={{ color: "#f59e0b" }} /></div>
        <div className="flex-1">
          <p className="text-sm font-bold">SplitWise Premium</p>
          <p className="text-xs text-muted-foreground">Early access · Ad-free · Unlimited · Cloud sync</p>
        </div>
        <span className="text-xs px-2 py-1 rounded-full font-bold" style={{ background: "#f59e0b", color: "#fff" }}>ACTIVE</span>
      </div>
    </div>
  );
}

// ─── Groups Screen ─────────────────────────────────────────────────────────────

function GroupsScreen({ groups, expenses, friends, onAddGroup }: { groups: Group[]; expenses: Expense[]; friends: Friend[]; onAddGroup: () => void }) {
  const [sel, setSel] = useState<Group | null>(null);

  if (sel) {
    const gExps = expenses.filter(e => e.groupId === sel.id);
    const members = sel.memberIds.map(id => friendById(id, friends));
    const totalUSD = gExps.reduce((s, e) => s + toUSD(e.amount, e.currency), 0);
    return (
      <div className="space-y-4">
        <button onClick={() => setSel(null)} className="flex items-center gap-2 text-sm font-bold" style={{ color: "#1a237e" }}>← Back to Groups</button>
        <div className="rounded-2xl p-4 text-white" style={{ background: sel.color }}>
          <div className="flex items-center gap-3 mb-3">
            <div className="w-12 h-12 rounded-2xl bg-white/20 flex items-center justify-center text-2xl">{sel.icon}</div>
            <div><h2 className="font-bold text-lg">{sel.name}</h2><p className="text-sm opacity-75">{sel.description}</p></div>
          </div>
          <div className="flex gap-5">
            <div><p className="text-xs opacity-60">Members</p><p className="font-bold">{members.length}</p></div>
            <div><p className="text-xs opacity-60">Expenses</p><p className="font-bold">{gExps.length}</p></div>
            <div><p className="text-xs opacity-60">Total Spent</p><p className="font-bold font-mono">{formatAmt(totalUSD)}</p></div>
          </div>
        </div>
        <div className="bg-card rounded-2xl border border-border p-4">
          <h3 className="text-sm font-bold mb-3">Members</h3>
          <div className="flex gap-2 flex-wrap">
            {members.map(m => (
              <div key={m.id} className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-secondary">
                <Avatar person={m} size={20} />
                <span className="text-xs font-medium">{m.id === "me" ? "You" : m.name.split(" ")[0]}</span>
              </div>
            ))}
          </div>
        </div>
        <div className="bg-card rounded-2xl border border-border p-4">
          <h3 className="text-sm font-bold mb-3">Group Expenses</h3>
          {gExps.length === 0 ? <p className="text-sm text-muted-foreground text-center py-4">No expenses yet</p> : (
            <div className="space-y-3">
              {gExps.map(exp => {
                const cat = getCatMeta(exp.category);
                return (
                  <div key={exp.id} className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-xl flex items-center justify-center" style={{ background: cat.color + "15" }}>{cat.icon}</div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-bold truncate">{exp.title}</p>
                      <p className="text-xs text-muted-foreground">{exp.date} · paid by {friendName(exp.paidById, friends)}</p>
                    </div>
                    <span className="text-sm font-bold font-mono">{formatAmt(exp.amount, exp.currency)}</span>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-bold">Groups</h2>
        <button onClick={onAddGroup} className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-white text-sm font-bold" style={{ background: "#1a237e" }}><Plus size={15} /> New Group</button>
      </div>
      {groups.length === 0 ? (
        <div className="flex flex-col items-center gap-3 py-12 text-center">
          <Users size={44} className="text-muted-foreground" />
          <p className="font-bold">No groups yet</p>
          <p className="text-sm text-muted-foreground">Create a group to split expenses together</p>
        </div>
      ) : (
        <div className="space-y-3">
          {groups.map(g => {
            const gExps = expenses.filter(e => e.groupId === g.id);
            const total = gExps.reduce((s, e) => s + toUSD(e.amount, e.currency), 0);
            const mems = g.memberIds.map(id => friendById(id, friends));
            return (
              <button key={g.id} onClick={() => setSel(g)} className="w-full bg-card rounded-2xl border border-border p-4 flex items-center gap-4 hover:shadow-md transition-all text-left active:scale-[0.99]">
                <div className="w-12 h-12 rounded-2xl flex items-center justify-center text-2xl shrink-0" style={{ background: g.color + "20" }}>{g.icon}</div>
                <div className="flex-1 min-w-0">
                  <p className="font-bold">{g.name}</p>
                  <p className="text-xs text-muted-foreground mb-1.5">{mems.length} members · {gExps.length} expenses</p>
                  <div className="flex -space-x-1.5">
                    {mems.slice(0, 4).map(m => <div key={m.id} className="w-5 h-5 rounded-full border-2 border-card flex items-center justify-center text-white" style={{ background: m.avatarColor, fontSize: 8 }}>{initials(m.name)}</div>)}
                  </div>
                </div>
                <div className="text-right shrink-0">
                  <p className="text-sm font-bold font-mono">{formatAmt(total)}</p>
                  <p className="text-xs text-muted-foreground">spent</p>
                  <ChevronRight size={15} className="text-muted-foreground ml-auto mt-1" />
                </div>
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}

// ─── Friends Screen ────────────────────────────────────────────────────────────

function FriendsScreen({ friends, balances, expenses, onAddFriend }: { friends: Friend[]; balances: Record<string, number>; expenses: Expense[]; onAddFriend: () => void }) {
  const [sel, setSel] = useState<Friend | null>(null);

  if (sel) {
    const shared = expenses.filter(e => e.splits[sel.id] !== undefined || e.paidById === sel.id);
    const b = balances[sel.id] ?? 0;
    return (
      <div className="space-y-4">
        <button onClick={() => setSel(null)} className="flex items-center gap-2 text-sm font-bold" style={{ color: "#1a237e" }}>← Back to Friends</button>
        <div className="bg-card rounded-2xl border border-border p-5 flex flex-col items-center gap-3">
          <Avatar person={sel} size={64} />
          <div className="text-center"><h2 className="font-bold text-lg">{sel.name}</h2><p className="text-sm text-muted-foreground">{sel.email}</p></div>
          <div className="flex gap-6 text-center">
            <div><p className="text-xs text-muted-foreground">Balance</p>
              <span className="font-mono font-bold text-sm flex items-center gap-1" style={{ color: b > 0 ? "#ff5252" : b < 0 ? "#00bfa5" : "#6b7280" }}>
                {b !== 0 && (b < 0 ? <ArrowUpRight size={13} /> : <ArrowDownLeft size={13} />)}{b === 0 ? "Settled" : formatAmt(Math.abs(b))}
              </span>
            </div>
            <div><p className="text-xs text-muted-foreground">Currency</p><p className="text-sm font-bold font-mono">{sel.currency}</p></div>
            <div><p className="text-xs text-muted-foreground">Shared</p><p className="text-sm font-bold">{shared.length}</p></div>
          </div>
          {b !== 0 && (
            <div className="flex gap-2 w-full">
              {["UPI", "PayPal", "Venmo"].map(p => (
                <button key={p} className="flex-1 py-2 rounded-xl border border-primary/30 text-primary text-xs font-bold hover:bg-primary/10 transition-colors">{p}</button>
              ))}
            </div>
          )}
        </div>
        <div className="bg-card rounded-2xl border border-border p-4">
          <h3 className="text-sm font-bold mb-3">Shared Expenses</h3>
          {shared.length === 0 ? <p className="text-sm text-muted-foreground text-center py-4">No shared expenses</p> : (
            <div className="space-y-3">
              {shared.map(exp => {
                const cat = getCatMeta(exp.category);
                return (
                  <div key={exp.id} className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-xl flex items-center justify-center" style={{ background: cat.color + "15" }}>{cat.icon}</div>
                    <div className="flex-1 min-w-0"><p className="text-sm font-bold truncate">{exp.title}</p><p className="text-xs text-muted-foreground">{exp.date}</p></div>
                    <span className="text-sm font-mono font-bold">{formatAmt(exp.splits[sel.id] ?? 0, exp.currency)}</span>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-bold">Friends</h2>
        <button onClick={onAddFriend} className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-white text-sm font-bold" style={{ background: "#1a237e" }}><UserPlus size={15} /> Add Friend</button>
      </div>
      {friends.length === 0 ? (
        <div className="flex flex-col items-center gap-3 py-12 text-center">
          <UserPlus size={44} className="text-muted-foreground" />
          <p className="font-bold">No friends yet</p>
          <p className="text-sm text-muted-foreground">Add friends to start splitting expenses</p>
        </div>
      ) : (
        <div className="space-y-2">
          {friends.map(f => {
            const b = balances[f.id] ?? 0;
            return (
              <button key={f.id} onClick={() => setSel(f)} className="w-full bg-card rounded-2xl border border-border p-4 flex items-center gap-3 hover:shadow-md transition-all text-left active:scale-[0.99]">
                <Avatar person={f} size={44} />
                <div className="flex-1 min-w-0"><p className="font-bold">{f.name}</p><p className="text-xs text-muted-foreground">{f.email}</p></div>
                <div className="text-right">
                  {b === 0 ? <span className="text-xs font-bold" style={{ color: "#00bfa5" }}>Settled</span> : (
                    <span className="font-mono font-bold text-sm flex items-center gap-1" style={{ color: b < 0 ? "#ff5252" : "#00bfa5" }}>
                      {b < 0 ? <ArrowDownLeft size={13} /> : <ArrowUpRight size={13} />}{formatAmt(Math.abs(b))}
                    </span>
                  )}
                  <ChevronRight size={15} className="text-muted-foreground block ml-auto mt-1" />
                </div>
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}

// ─── Activity Screen ───────────────────────────────────────────────────────────

function ActivityScreen({ expenses, friends, onDelete }: { expenses: Expense[]; friends: Friend[]; onDelete: (id: string) => void }) {
  const [search, setSearch] = useState("");
  const [filterCat, setFilterCat] = useState<Category | "">("");
  const [showImport, setShowImport] = useState(false);
  const [imported, setImported] = useState(false);

  const filtered = useMemo(() => {
    let list = [...expenses].sort((a, b) => b.date.localeCompare(a.date));
    if (search) {
      const q = search.toLowerCase();
      list = list.filter(e => e.title.toLowerCase().includes(q) || e.category.toLowerCase().includes(q) || friendName(e.paidById, friends).toLowerCase().includes(q) || e.amount.toString().includes(q));
    }
    if (filterCat) list = list.filter(e => e.category === filterCat);
    return list;
  }, [expenses, search, filterCat, friends]);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-bold">Activity</h2>
        <button onClick={() => setShowImport(true)} className="flex items-center gap-1.5 px-3 py-2 rounded-xl border border-border text-sm font-bold hover:bg-secondary transition-colors"><Upload size={14} /> Import</button>
      </div>
      <div className="relative">
        <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
        <input className="w-full bg-card border border-border rounded-xl pl-9 pr-9 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20" placeholder="Search by name, amount, category…" value={search} onChange={e => setSearch(e.target.value)} />
        {search && <button onClick={() => setSearch("")} className="absolute right-3 top-1/2 -translate-y-1/2"><X size={14} className="text-muted-foreground" /></button>}
      </div>
      <div className="flex gap-2 overflow-x-auto pb-0.5" style={{ scrollbarWidth: "none" }}>
        <button onClick={() => setFilterCat("")} className="shrink-0 px-3 py-1.5 rounded-full text-xs font-bold transition-all" style={{ background: !filterCat ? "#1a237e" : "#eef0f8", color: !filterCat ? "#fff" : "#6b7280" }}>All</button>
        {CATEGORIES.map(cat => (
          <button key={cat.name} onClick={() => setFilterCat(filterCat === cat.name ? "" : cat.name)}
            className="shrink-0 px-3 py-1.5 rounded-full text-xs font-bold transition-all"
            style={{ background: filterCat === cat.name ? cat.color : cat.color + "15", color: filterCat === cat.name ? "#fff" : cat.color }}>
            {cat.icon} {cat.name}
          </button>
        ))}
      </div>
      {filtered.length === 0 ? (
        <div className="flex flex-col items-center gap-3 py-12 text-center">
          <Search size={44} className="text-muted-foreground" />
          <p className="font-bold">No expenses found</p>
          <p className="text-sm text-muted-foreground">Try a different search or filter</p>
        </div>
      ) : (
        <div className="space-y-2">
          {filtered.map(exp => {
            const cat = getCatMeta(exp.category);
            const myShare = exp.splits["me"] ?? 0;
            return (
              <div key={exp.id} className="bg-card rounded-2xl border border-border p-4">
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 rounded-xl flex items-center justify-center text-lg shrink-0" style={{ background: cat.color + "15" }}>{cat.icon}</div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-1.5 mb-0.5">
                      <p className="font-bold text-sm truncate">{exp.title}</p>
                      {exp.isRecurring && <span className="shrink-0 flex items-center gap-0.5 text-xs px-1.5 py-0.5 rounded-full font-bold" style={{ background: "#00bfa515", color: "#00bfa5" }}><Repeat size={9} /> {exp.recurringInterval}</span>}
                    </div>
                    <div className="flex items-center gap-2 flex-wrap mb-1">
                      <CategoryBadge category={exp.category} />
                      <span className="text-xs text-muted-foreground">{exp.date}</span>
                    </div>
                    <p className="text-xs text-muted-foreground">Paid by {friendName(exp.paidById, friends)} · {exp.splitType} split</p>
                  </div>
                  <div className="text-right shrink-0">
                    <p className="font-bold font-mono text-sm">{formatAmt(exp.amount, exp.currency)}</p>
                    <p className="text-xs text-muted-foreground">your share</p>
                    <p className="text-xs font-mono font-bold">{formatAmt(myShare, exp.currency)}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2 mt-3 pt-3 border-t border-border">
                  <div className="flex -space-x-1.5 flex-1">
                    {Object.keys(exp.splits).slice(0, 5).map(id => { const p = friendById(id, friends); return <Avatar key={id} person={p} size={20} />; })}
                  </div>
                  <button onClick={() => onDelete(exp.id)} className="p-1.5 rounded-lg hover:bg-destructive/10 transition-colors"><Trash2 size={14} className="text-destructive" /></button>
                </div>
              </div>
            );
          })}
        </div>
      )}
      <Modal open={showImport} onClose={() => { setShowImport(false); setImported(false); }} title="Import Expenses">
        <div className="space-y-4">
          <div className="border-2 border-dashed border-border rounded-xl p-8 flex flex-col items-center gap-3 text-center cursor-pointer hover:border-accent/50 transition-colors" onClick={() => setImported(true)}>
            <Upload size={32} className="text-muted-foreground" />
            <p className="font-bold text-sm">Drop CSV or bank statement here</p>
            <p className="text-xs text-muted-foreground">Supports CSV, OFX, QFX formats</p>
            <button className="px-4 py-2 rounded-xl bg-secondary text-sm font-bold hover:bg-muted transition-colors">Browse Files</button>
          </div>
          {imported && (
            <div className="flex items-center gap-3 p-3 rounded-xl" style={{ background: "#00bfa515", border: "1px solid #00bfa530" }}>
              <CheckCircle size={18} style={{ color: "#00bfa5" }} />
              <div><p className="text-sm font-bold" style={{ color: "#00bfa5" }}>Import successful!</p><p className="text-xs text-muted-foreground">12 transactions imported from statement.csv</p></div>
            </div>
          )}
          <div>
            <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-2">Connect payment apps</p>
            <div className="grid grid-cols-3 gap-2">
              {["UPI", "PayPal", "Venmo"].map(p => <button key={p} className="py-2.5 rounded-xl border border-border text-sm font-bold hover:bg-secondary transition-colors">{p}</button>)}
            </div>
          </div>
        </div>
      </Modal>
    </div>
  );
}

// ─── Analytics Screen ──────────────────────────────────────────────────────────

function AnalyticsScreen({ expenses, friends }: { expenses: Expense[]; friends: Friend[] }) {
  const [period, setPeriod] = useState<"month" | "all">("month");

  const catData = useMemo(() => {
    const map: Record<string, number> = {};
    expenses.forEach(e => { map[e.category] = (map[e.category] ?? 0) + toUSD(e.amount, e.currency); });
    return Object.entries(map).map(([name, value]) => ({ name, value: parseFloat(value.toFixed(2)), color: getCatMeta(name as Category).color })).sort((a, b) => b.value - a.value);
  }, [expenses]);

  const monthlyData = useMemo(() => {
    const bases = [820, 1240, 950, 1680, 2100, 0];
    const juneTotal = expenses.reduce((s, e) => s + toUSD(e.amount, e.currency), 0);
    return ["Jan", "Feb", "Mar", "Apr", "May", "Jun"].map((m, i) => ({ month: m, amount: i === 5 ? Math.round(juneTotal) : bases[i] }));
  }, [expenses]);

  const personData = useMemo(() => {
    return [ME, ...friends].map(p => {
      const paid = expenses.filter(e => e.paidById === p.id).reduce((s, e) => s + toUSD(e.amount, e.currency), 0);
      return { name: p.id === "me" ? "You" : p.name.split(" ")[0], amount: parseFloat(paid.toFixed(2)), color: p.avatarColor };
    }).filter(d => d.amount > 0);
  }, [expenses, friends]);

  const total = expenses.reduce((s, e) => s + toUSD(e.amount, e.currency), 0);
  const avg = expenses.length ? total / expenses.length : 0;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-bold">Analytics</h2>
        <div className="flex bg-muted rounded-xl p-1">
          {(["month", "all"] as const).map(p => (
            <button key={p} onClick={() => setPeriod(p)} className="px-3 py-1 rounded-lg text-xs font-bold capitalize transition-all"
              style={{ background: period === p ? "#1a237e" : "transparent", color: period === p ? "#fff" : "#6b7280" }}>
              {p === "month" ? "This Month" : "All Time"}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-2 gap-2.5">
        {[
          { label: "Total Spent", value: formatAmt(total), icon: <DollarSign size={15} />, color: "#1a237e" },
          { label: "Avg per Expense", value: formatAmt(avg), icon: <TrendingUp size={15} />, color: "#00bfa5" },
          { label: "Transactions", value: expenses.length.toString(), icon: <Activity size={15} />, color: "#ff8f00" },
          { label: "Categories", value: catData.length.toString(), icon: <Tag size={15} />, color: "#7c3aed" },
        ].map(c => (
          <div key={c.label} className="bg-card rounded-2xl border border-border p-4">
            <div className="w-8 h-8 rounded-xl flex items-center justify-center text-white mb-2" style={{ background: c.color }}>{c.icon}</div>
            <p className="text-lg font-bold font-mono">{c.value}</p>
            <p className="text-xs text-muted-foreground">{c.label}</p>
          </div>
        ))}
      </div>

      <div className="bg-card rounded-2xl border border-border p-4">
        <h3 className="text-sm font-bold mb-4">Monthly Spending Trend</h3>
        <ResponsiveContainer width="100%" height={150}>
          <BarChart data={monthlyData} barSize={26}>
            <CartesianGrid strokeDasharray="3 3" stroke="#eef0f8" vertical={false} />
            <XAxis dataKey="month" tick={{ fontSize: 11, fill: "#6b7280" }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fontSize: 11, fill: "#6b7280" }} axisLine={false} tickLine={false} tickFormatter={v => `$${v}`} width={44} />
            <Tooltip formatter={(v: number) => [formatAmt(v), "Spent"]} contentStyle={{ borderRadius: 12, border: "1px solid #eef0f8", fontSize: 12 }} />
            <Bar dataKey="amount" fill="#1a237e" radius={[6, 6, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="bg-card rounded-2xl border border-border p-4">
        <h3 className="text-sm font-bold mb-4">Spending by Category</h3>
        <div className="flex gap-4 items-center">
          <div style={{ width: 130, height: 130, flexShrink: 0 }}>
            <ResponsiveContainer width="100%" height="100%">
              <RePie>
                <Pie data={catData} dataKey="value" cx="50%" cy="50%" innerRadius={32} outerRadius={58} paddingAngle={3}>
                  {catData.map((e, i) => <Cell key={i} fill={e.color} />)}
                </Pie>
                <Tooltip formatter={(v: number) => formatAmt(v)} contentStyle={{ borderRadius: 12, fontSize: 12 }} />
              </RePie>
            </ResponsiveContainer>
          </div>
          <div className="flex-1 space-y-2">
            {catData.map(c => (
              <div key={c.name} className="flex items-center gap-2">
                <div className="w-2 h-2 rounded-full shrink-0" style={{ background: c.color }} />
                <span className="text-xs flex-1 truncate font-medium">{c.name}</span>
                <span className="text-xs font-mono font-bold">{formatAmt(c.value)}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="bg-card rounded-2xl border border-border p-4">
        <h3 className="text-sm font-bold mb-4">Paid by Person</h3>
        <ResponsiveContainer width="100%" height={130}>
          <BarChart data={personData} layout="vertical" barSize={18}>
            <CartesianGrid strokeDasharray="3 3" stroke="#eef0f8" horizontal={false} />
            <XAxis type="number" tick={{ fontSize: 11, fill: "#6b7280" }} axisLine={false} tickLine={false} tickFormatter={v => `$${v}`} />
            <YAxis type="category" dataKey="name" tick={{ fontSize: 11, fill: "#6b7280" }} axisLine={false} tickLine={false} width={38} />
            <Tooltip formatter={(v: number) => [formatAmt(v), "Paid"]} contentStyle={{ borderRadius: 12, border: "1px solid #eef0f8", fontSize: 12 }} />
            <Bar dataKey="amount" radius={[0, 6, 6, 0]}>{personData.map((e, i) => <Cell key={i} fill={e.color} />)}</Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="bg-card rounded-2xl border border-border p-4">
        <h3 className="text-sm font-bold mb-3">Category Breakdown</h3>
        <div className="space-y-3">
          {catData.map(c => {
            const pct = total > 0 ? (c.value / total) * 100 : 0;
            const meta = getCatMeta(c.name as Category);
            return (
              <div key={c.name}>
                <div className="flex items-center justify-between mb-1">
                  <span className="text-xs font-bold flex items-center gap-1">{meta.icon} {c.name}</span>
                  <span className="text-xs font-mono font-bold">{formatAmt(c.value)} <span className="text-muted-foreground font-normal">({pct.toFixed(0)}%)</span></span>
                </div>
                <div className="h-1.5 bg-muted rounded-full overflow-hidden">
                  <div className="h-full rounded-full" style={{ width: `${pct}%`, background: c.color }} />
                </div>
              </div>
            );
          })}
        </div>
      </div>

      <div className="bg-card rounded-2xl border border-border p-4 space-y-3">
        <h3 className="text-sm font-bold">App Settings</h3>
        {[
          { icon: <Globe size={15} className="text-muted-foreground" />, label: "Language", control: <select className="bg-secondary border border-border rounded-lg px-2 py-1 text-xs font-bold">{LANGUAGES.map(l => <option key={l}>{l}</option>)}</select> },
          { icon: <DollarSign size={15} className="text-muted-foreground" />, label: "Base Currency", control: <select className="bg-secondary border border-border rounded-lg px-2 py-1 text-xs font-bold">{CURRENCIES.slice(0, 10).map(c => <option key={c.code}>{c.code}</option>)}</select> },
          { icon: <Shield size={15} className="text-muted-foreground" />, label: "Cloud Sync", control: <span className="text-xs px-2 py-1 rounded-full font-bold" style={{ background: "#00bfa520", color: "#00bfa5" }}>Active</span> },
          { icon: <Wallet size={15} className="text-muted-foreground" />, label: "Offline Mode", control: <span className="text-xs px-2 py-1 rounded-full font-bold bg-muted text-muted-foreground">Available</span> },
        ].map(r => (
          <div key={r.label} className="flex items-center justify-between">
            <div className="flex items-center gap-2">{r.icon}<span className="text-sm">{r.label}</span></div>
            {r.control}
          </div>
        ))}
      </div>
    </div>
  );
}

// ─── Bottom Nav ────────────────────────────────────────────────────────────────

const NAV = [
  { id: "home" as View, label: "Home", Icon: Home },
  { id: "groups" as View, label: "Groups", Icon: Users },
  { id: "friends" as View, label: "Friends", Icon: UserPlus },
  { id: "activity" as View, label: "Activity", Icon: Activity },
  { id: "analytics" as View, label: "Analytics", Icon: BarChart2 },
];

// ─── App ──────────────────────────────────────────────────────────────────────

export default function App() {
  const [view, setView] = useState<View>("home");
  const [friends, setFriends] = useState<Friend[]>(SEED_FRIENDS);
  const [groups, setGroups] = useState<Group[]>(SEED_GROUPS);
  const [expenses, setExpenses] = useState<Expense[]>(SEED_EXPENSES);
  const [showAddExp, setShowAddExp] = useState(false);
  const [showAddGrp, setShowAddGrp] = useState(false);
  const [showAddFrd, setShowAddFrd] = useState(false);
  const [showDebt, setShowDebt] = useState(false);

  const balances = useMemo(() => computeBalances(expenses, friends), [expenses, friends]);

  return (
    <div className="size-full flex items-center justify-center" style={{ background: "#d1d5e8" }}>
      <div className="relative flex flex-col overflow-hidden"
        style={{ width: "min(390px,100vw)", height: "min(844px,100vh)", background: "#f4f6fb", borderRadius: window.innerWidth > 390 ? "44px" : "0", boxShadow: "0 32px 80px rgba(0,0,0,0.28)", fontFamily: "'Plus Jakarta Sans',system-ui,sans-serif" }}>

        {/* Status bar */}
        <div className="flex items-center justify-between px-6 pt-3.5 pb-2 shrink-0" style={{ background: "#1a237e" }}>
          <span className="text-white text-xs font-bold font-mono opacity-90">9:41</span>
          <div className="flex items-center gap-2">
            <Bell size={13} className="text-white opacity-75" />
            <Settings size={13} className="text-white opacity-75" />
            <div className="flex items-end gap-0.5">{[3, 5, 7].map(h => <div key={h} className="w-1 bg-white rounded-sm opacity-90" style={{ height: h }} />)}</div>
          </div>
        </div>

        {/* App header */}
        <div className="px-5 py-3 shrink-0" style={{ background: "#1a237e" }}>
          <div className="flex items-center justify-between">
            <div><p className="text-white/60 text-xs font-semibold">Welcome back 👋</p><h1 className="text-white font-extrabold text-xl leading-tight tracking-tight">SplitWise</h1></div>
            <div className="flex items-center gap-2.5">
              <span className="text-xs px-2 py-1 rounded-full font-extrabold flex items-center gap-1" style={{ background: "#f59e0b", color: "#fff" }}><Star size={10} />PRO</span>
              <Avatar person={ME} size={34} />
            </div>
          </div>
        </div>

        {/* Scrollable content */}
        <div className="flex-1 overflow-y-auto px-4 py-4" style={{ scrollbarWidth: "none" }}>
          {view === "home" && <HomeScreen expenses={expenses} friends={friends} groups={groups} balances={balances} onDebt={() => setShowDebt(true)} onAdd={() => setShowAddExp(true)} />}
          {view === "groups" && <GroupsScreen groups={groups} expenses={expenses} friends={friends} onAddGroup={() => setShowAddGrp(true)} />}
          {view === "friends" && <FriendsScreen friends={friends} balances={balances} expenses={expenses} onAddFriend={() => setShowAddFrd(true)} />}
          {view === "activity" && <ActivityScreen expenses={expenses} friends={friends} onDelete={id => setExpenses(p => p.filter(e => e.id !== id))} />}
          {view === "analytics" && <AnalyticsScreen expenses={expenses} friends={friends} />}
        </div>

        {/* FAB */}
        {(view === "home" || view === "activity") && (
          <button onClick={() => setShowAddExp(true)}
            className="absolute bottom-20 right-4 w-14 h-14 rounded-full flex items-center justify-center text-white z-20 active:scale-95 transition-all"
            style={{ background: "linear-gradient(135deg,#1a237e,#3949ab)", boxShadow: "0 6px 24px rgba(26,35,126,0.45)" }}>
            <Plus size={24} />
          </button>
        )}

        {/* Bottom nav */}
        <div className="shrink-0 flex items-center justify-around bg-card border-t border-border" style={{ paddingTop: 10, paddingBottom: 18 }}>
          {NAV.map(({ id, label, Icon }) => {
            const active = view === id;
            return (
              <button key={id} onClick={() => setView(id)} className="flex flex-col items-center gap-0.5 px-2 transition-all" style={{ color: active ? "#1a237e" : "#9ca3af" }}>
                <div className={`p-1.5 rounded-xl transition-all ${active ? "bg-blue-50" : ""}`}><Icon size={20} strokeWidth={active ? 2.5 : 1.8} /></div>
                <span className="text-xs font-bold">{label}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Modals */}
      <AddExpenseModal open={showAddExp} onClose={() => setShowAddExp(false)} friends={friends} groups={groups} onAdd={e => setExpenses(p => [e, ...p])} />
      <AddGroupModal open={showAddGrp} onClose={() => setShowAddGrp(false)} friends={friends} onAdd={g => setGroups(p => [...p, g])} />
      <AddFriendModal open={showAddFrd} onClose={() => setShowAddFrd(false)} onAdd={f => setFriends(p => [...p, f])} />
      <DebtModal open={showDebt} onClose={() => setShowDebt(false)} balances={balances} friends={friends} />
    </div>
  );
}
