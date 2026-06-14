interface BalanceIndicatorProps {
  mediaAmarelo: number;
  mediaPreto: number;
}

export function BalanceIndicator({ mediaAmarelo, mediaPreto }: BalanceIndicatorProps) {
  const diff = Math.abs(mediaAmarelo - mediaPreto);

  let color: string;
  let label: string;

  if (diff <= 0.5) {
    color = 'bg-green-500';
    label = 'Equilibrado';
  } else if (diff <= 1.0) {
    color = 'bg-yellow-500';
    label = 'Leve desequilíbrio';
  } else {
    color = 'bg-red-500';
    label = 'Desequilibrado';
  }

  return (
    <div className="flex items-center gap-2">
      <span className={`w-3 h-3 rounded-full ${color}`} />
      <span className="text-sm text-gavioes-white/80">
        {label} (Δ {diff.toFixed(2)})
      </span>
    </div>
  );
}
