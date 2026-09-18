<script>
	import RoleGuard from '$lib/components/RoleGuard.svelte';
	import { currentUser } from '$lib/stores/auth.js';
	import { formatCurrency } from '$lib/utils/formatters.js';

	const user = $derived($currentUser);
</script>

<svelte:head>
	<title>Corporate Sponsor Dashboard | Shared Services Hub</title>
</svelte:head>

<RoleGuard allowedRoles={['CORPORATE', 'ADMIN']}>
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		<!-- Header -->
		<div class="rounded-3xl border border-emerald-200 bg-gradient-to-r from-slate-900 via-slate-800 to-emerald-950 p-8 text-white shadow-xl mb-8">
			<div class="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
				<div>
					<div class="inline-flex items-center gap-1.5 rounded-full bg-emerald-500/20 px-3 py-1 text-xs font-bold text-emerald-300 border border-emerald-400/30 mb-2">
						<span>🛡 Corporate Enterprise Development Sponsor</span>
					</div>
					<h1 class="text-3xl font-extrabold tracking-tight">
						Welcome, {user?.name || 'Corporate Partner'}
					</h1>
					<p class="mt-1 text-sm text-slate-300 max-w-xl">
						Monitor youth wage disbursements, SME capacity development, and download verified audit reports for B-BBEE scorecard compliance.
					</p>
				</div>

				<div class="flex items-center gap-3">
					<button
						type="button"
						class="rounded-xl bg-emerald-500 px-4 py-2.5 text-xs font-bold text-slate-950 shadow-md hover:bg-emerald-400 transition-all"
					>
						Download B-BBEE Audit Pack
					</button>
				</div>
			</div>

			<!-- Metrics Row -->
			<div class="mt-8 pt-6 border-t border-slate-700/60 grid grid-cols-2 sm:grid-cols-4 gap-6">
				<div>
					<div class="text-xs text-slate-400 font-semibold uppercase tracking-wider">Cohort Youth Funded</div>
					<div class="text-3xl font-black mt-1 text-white">45 Candidates</div>
				</div>
				<div>
					<div class="text-xs text-slate-400 font-semibold uppercase tracking-wider">Wages Disbursed</div>
					<div class="text-3xl font-black mt-1 text-emerald-400">{formatCurrency(385000)}</div>
				</div>
				<div>
					<div class="text-xs text-slate-400 font-semibold uppercase tracking-wider">SMEs Supported</div>
					<div class="text-3xl font-black mt-1 text-white">28 Businesses</div>
				</div>
				<div>
					<div class="text-xs text-slate-400 font-semibold uppercase tracking-wider">B-BBEE Scorecard Points</div>
					<div class="text-3xl font-black mt-1 text-amber-300">25 / 25 Pts</div>
				</div>
			</div>
		</div>

		<!-- Impact Visuals -->
		<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
			<div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-xs">
				<h3 class="text-lg font-bold text-slate-900 mb-2">Cohort Skill Distribution</h3>
				<p class="text-xs text-slate-500 mb-6">Current allocation of sponsored candidates across SME industry sectors.</p>
				<div class="space-y-3">
					<div>
						<div class="flex justify-between text-xs font-semibold text-slate-700 mb-1">
							<span>Digital & E-Commerce Operations</span>
							<span>40%</span>
						</div>
						<div class="h-2 rounded-full bg-slate-100 overflow-hidden">
							<div class="h-full bg-indigo-600 rounded-full" style="width: 40%"></div>
						</div>
					</div>
					<div>
						<div class="flex justify-between text-xs font-semibold text-slate-700 mb-1">
							<span>Accounting & Financial Reconciliations</span>
							<span>28%</span>
						</div>
						<div class="h-2 rounded-full bg-slate-100 overflow-hidden">
							<div class="h-full bg-emerald-500 rounded-full" style="width: 28%"></div>
						</div>
					</div>
					<div>
						<div class="flex justify-between text-xs font-semibold text-slate-700 mb-1">
							<span>Marketing & Content Design</span>
							<span>22%</span>
						</div>
						<div class="h-2 rounded-full bg-slate-100 overflow-hidden">
							<div class="h-full bg-amber-500 rounded-full" style="width: 22%"></div>
						</div>
					</div>
					<div>
						<div class="flex justify-between text-xs font-semibold text-slate-700 mb-1">
							<span>Technical & IT Support</span>
							<span>10%</span>
						</div>
						<div class="h-2 rounded-full bg-slate-100 overflow-hidden">
							<div class="h-full bg-purple-500 rounded-full" style="width: 10%"></div>
						</div>
					</div>
				</div>
			</div>

			<div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-xs">
				<h3 class="text-lg font-bold text-slate-900 mb-2">Verified Ledger Compliance</h3>
				<p class="text-xs text-slate-500 mb-6">Every cent disbursed is backed by an immutable SME sign-off.</p>
				<div class="rounded-2xl border border-emerald-100 bg-emerald-50/50 p-4 mb-4">
					<div class="flex items-center gap-2 text-emerald-800 font-bold text-sm">
						<svg class="h-5 w-5 text-emerald-600" viewBox="0 0 20 20" fill="currentColor">
							<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd" />
						</svg>
						<span>100% Verification Rate</span>
					</div>
					<p class="mt-1 text-xs text-emerald-700">
						Wages are held in trust and disbursed strictly after dual digital verification by both candidate and enterprise.
					</p>
				</div>
				<div class="text-center pt-2">
					<a href="/tasks" class="text-xs font-bold text-indigo-600 hover:text-indigo-800">
						View Platform Tasks in Execution →
					</a>
				</div>
			</div>
		</div>
	</div>
</RoleGuard>
